package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.Config;
import com.github.codecnomad.codecclient.Module;
import com.github.codecnomad.codecclient.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EntityEsp extends Module {
    private final HashSet<Entity> entityList = new HashSet<>();

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        entityList.clear();
        for (Entity entity : event.world.loadedEntityList) {
            checkAndAdd(entity);
        }
    }

    @SubscribeEvent()
    public void entityDeath(LivingDeathEvent event) {
        entityList.remove(event.entity);
    }

    @SubscribeEvent()
    public void entitySpawn(EntityJoinWorldEvent event) {
        checkAndAdd(event.entity);
    }

    @SubscribeEvent()
    public void renderLast(RenderWorldLastEvent event) {
        final HashSet<Entity> copiedEntityList = new HashSet<>(entityList);
        for (Entity entity : copiedEntityList) {
            if (!entity.isEntityAlive()) {
                entityList.remove(entity);
                continue;
            }
            GlStateManager.disableDepth();
            AxisAlignedBB entityBB = entity.getEntityBoundingBox();
            RenderUtils.drawOutlinedFilledBoundingBox(
                new AxisAlignedBB(
                    entityBB.minX - Config.EntityEspWidth,
                    entityBB.minY,
                    entityBB.minZ - Config.EntityEspWidth,
                    entityBB.maxX + Config.EntityEspWidth,
                    entityBB.maxY,
                    entityBB.maxZ + Config.EntityEspWidth
                ),
                Color.getHSBColor(Config.EntityEspColorH, Config.EntityEspColorS, Config.EntityEspColorB),
                event.partialTicks
            );
            GlStateManager.enableDepth();
        }
    }

    private void checkAndAdd(Entity entity) {
        String entityName = entity.getName();

        for (String name : Config.EntityEspWhitelist.split(";")) {
            if (StringUtils.containsIgnoreCase(entityName, name)) {
                entityList.add(entity);
                break;
            }
        }
    }
}
