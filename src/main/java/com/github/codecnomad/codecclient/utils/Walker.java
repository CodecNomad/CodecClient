package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.ui.Config;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Walker {
    List<BlockPos> wayPoints;
    Runnable callback;
    int currentPoint = 0;

    public void start() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void stop() {
        currentPoint = 0;
        MinecraftForge.EVENT_BUS.unregister(this);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindJump.getKeyCode(), false);
        callback.run();
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (currentPoint >= wayPoints.size()) {
            stop();
            return;
        }

        for (int i = currentPoint; i < wayPoints.size(); i++) {
            BlockPos iPos = wayPoints.get(i);
            BlockPos pPos = Client.mc.thePlayer.getPosition();
            if (Client.mc.theWorld.rayTraceBlocks(new Vec3(iPos.getX(), iPos.getY(), iPos.getZ()), new Vec3(pPos.getX(), pPos.getY(), pPos.getZ())) == null) {
                currentPoint = i;
            }
        }

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindForward.getKeyCode(), true);

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindJump.getKeyCode(), java.lang.Math.abs(Client.mc.thePlayer.motionX) + java.lang.Math.abs(Client.mc.thePlayer.motionZ) < 0.05 && wayPoints.get(currentPoint).getY() > Client.mc.thePlayer.posY);

        Client.rotation.setYaw(Math.getYaw(wayPoints.get(currentPoint)), Config.RotationSmoothing);
        Client.rotation.setPitch(Math.getPitch(wayPoints.get(currentPoint)), Config.RotationSmoothing);

        if (Client.mc.thePlayer.getDistanceSq(wayPoints.get(currentPoint)) < 2) {
            currentPoint++;
        }
    }

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (currentPoint < wayPoints.size()) {
            BlockPos waypoint = wayPoints.get(currentPoint);
            Render.drawOutlinedFilledBoundingBox(waypoint, Config.VisualColor.toJavaColor(), event.partialTicks);
            Render.draw3DString(new Vec3(waypoint.getX(), waypoint.getY(), waypoint.getZ()), String.format("Waypoint: %s", currentPoint), 0, event.partialTicks);
        }
    }

    public Walker(List<BlockPos> wayPoints, Runnable callback) {
        this.wayPoints = wayPoints;
        this.callback = callback;
    }
}
