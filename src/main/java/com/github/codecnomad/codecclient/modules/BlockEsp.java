package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.Config;
import com.github.codecnomad.codecclient.Module;
import com.github.codecnomad.codecclient.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Vector;

public class BlockEsp extends Module {
    // Todo: Make me multi-threaded daddy
    Vector<BlockPos> positions = new Vector<>();
    int counter = 0;
    @SubscribeEvent
    public void clientTick (TickEvent.ClientTickEvent event) {
        if (counter < Config.BlockUpdate || CodecClient.mc.thePlayer == null) {counter++;return;} counter = 0;

        int radius = Config.BlockEspRadius;

        positions.clear();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = CodecClient.mc.thePlayer.getPosition().add(x, y, z);
                    Block currentBlock = CodecClient.mc.theWorld.getBlockState(blockPos).getBlock();

                    boolean isWhitelisted = false;
                    if (!Config.BlockEspWhitelist.isEmpty()) {
                        for (String whitelist : Config.BlockEspWhitelist.split(";")) {
                            if (currentBlock.toString().contains(whitelist)) {
                                isWhitelisted = true;
                                break;
                            }
                        }
                    } else {
                        continue;
                    }

                    if (!isWhitelisted) {
                        continue;
                    }

                    positions.add(blockPos);
                }
            }
        }
    }

    @SubscribeEvent
    public void renderTick(RenderWorldLastEvent event) {
        for (BlockPos pos : positions) {
            GlStateManager.disableDepth();
            RenderUtils.drawOutlinedFilledBoundingBox(pos, Color.blue, event.partialTicks);
            GlStateManager.enableDepth();
        }
    }
}
