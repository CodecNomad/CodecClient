package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.Config;
import com.github.codecnomad.codecclient.Module;
import com.github.codecnomad.codecclient.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockEsp extends Module {
    private final HashMap<Block, List<BlockPos>>  blocks = new HashMap<>();
    public static boolean shouldUpdateBlocks = false;
    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        shouldUpdateBlocks = true;
    }

    @SubscribeEvent
    public void blockUpdate(BlockEvent event) {
        if (!(event instanceof BlockEvent.BreakEvent) && !(event instanceof BlockEvent.NeighborNotifyEvent)) {
            return;
        }

        Block block = event.state.getBlock();
        BlockPos pos = event.pos;
        Block newBlock = CodecClient.mc.theWorld.getBlockState(pos).getBlock();

        if (blocks.containsKey(block)) {
            blocks.get(block).remove(pos);
        }

        if (!blocks.containsKey(newBlock)) {
            blocks.put(newBlock, new ArrayList<>());
        }
        blocks.get(newBlock).add(pos);
    }
    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (shouldUpdateBlocks) {
            shouldUpdateBlocks = false;
            int radius = Config.BlockEspRadius;
            final HashMap<Block, List<BlockPos>> buffer = new HashMap<>();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = new BlockPos(
                                event.player.posX + x,
                                event.player.posY + y,
                                event.player.posZ + z
                        );
                        Block block = event.player.getEntityWorld().getBlockState(pos).getBlock();

                        if (!buffer.containsKey(block)) {
                            buffer.put(block, new ArrayList<>());
                        }
                        buffer.get(block).add(pos);
                    }
                }
            }
            blocks.clear();
            blocks.putAll(buffer);
        }
    }

    @SubscribeEvent
    public void renderTick(RenderWorldLastEvent event) {
        if (blocks == null) {return;}
        for (String name : Config.BlockEspWhitelist.split(";")) {
            if (blocks.containsKey(Block.getBlockFromName("minecraft:" + name))) {
                for (BlockPos pos : blocks.get(Block.getBlockFromName("minecraft:" + name))) {
                    GlStateManager.disableDepth();
                    RenderUtils.drawOutlinedFilledBoundingBox(pos, Color.blue, event.partialTicks);
                    GlStateManager.enableDepth();
                }
            }
        }
    }
}
