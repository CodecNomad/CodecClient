package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.Config;
import com.github.codecnomad.codecclient.Module;
import com.github.codecnomad.codecclient.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class EntityEsp extends Module {
    @SubscribeEvent
    public void renderTick(RenderWorldLastEvent event) {
        for (Entity entity : CodecClient.mc.theWorld.loadedEntityList) {
            if (entity == CodecClient.mc.thePlayer || entity.getName() == null) {
                continue;
            }

            String entityName = entity.getName().toLowerCase();
            String customName = entity.getCustomNameTag();
            boolean hasCustomName = entity.hasCustomName();

            boolean isWhitelisted = false;

            if (!Config.EntityEspWhitelist.isEmpty()) {
                for (String whitelist : Config.EntityEspWhitelist.split(";")) {
                    if (entityName.contains(whitelist.toLowerCase()) ||
                            (hasCustomName && customName.toLowerCase().contains(whitelist.toLowerCase()))) {
                        isWhitelisted = true;
                        break;
                    }
                }
            }
            else {
                isWhitelisted = true;
            }

            if (!isWhitelisted) {
                continue;
            }

            GlStateManager.disableDepth();
            AxisAlignedBB alignedBB = new AxisAlignedBB(
                    entity.posX - 0.5,
                    entity.posY,
                    entity.posZ - 0.5,
                    entity.posX + 0.5,
                    entity instanceof EntityArmorStand ? entity.posY - 2 : entity.posY + 2,
                    entity.posZ + 0.5
            );
            RenderUtils.drawOutlinedFilledBoundingBox(alignedBB, Color.blue, event.partialTicks);

            String displayName = "§bName: §4" + entity.getName().replaceAll("§[A-Za-z]", "").replaceAll("§[0-9]", "");
            RenderUtils.draw3DString(
                    new Vec3(
                            entity.posX,
                            entity instanceof EntityArmorStand ? entity.posY - 1 + 0.2 : entity.posY + 1 + 0.2,
                            entity.posZ
                    ),
                    displayName,
                    255,
                    event.partialTicks
            );
            String distance = "§bDistance: §4" + (int) entity.getDistanceToEntity(CodecClient.mc.thePlayer) + " §bblocks";
            RenderUtils.draw3DString(
                    new Vec3(
                            entity.posX,
                            entity instanceof EntityArmorStand ? entity.posY - 1 : entity.posY + 1,
                            entity.posZ
                    ),
                    distance,
                    255,
                    event.partialTicks
            );
            GlStateManager.enableDepth();
        }
    }
}

