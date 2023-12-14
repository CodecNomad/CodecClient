package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindBack.getKeyCode(), false);
        callback.run();
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (currentPoint >= wayPoints.size()) {
            stop();
            return;
        }

        float yawDifference = MathHelper.wrapAngleTo180_float(Math.getYaw(wayPoints.get(currentPoint)) - MathHelper.wrapAngleTo180_float(Client.mc.thePlayer.rotationYaw));

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindForward.getKeyCode(), yawDifference > -67.5 && yawDifference <= 67.5);

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindLeft.getKeyCode(), yawDifference > -157.5 && yawDifference <= -22.5);

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindRight.getKeyCode(), yawDifference > 22.5 && yawDifference <= 157.5);

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindBack.getKeyCode(), (yawDifference > -180 && yawDifference <= -157.5) || (yawDifference > 157.5 && yawDifference <= 180));

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindJump.getKeyCode(), java.lang.Math.abs(Client.mc.thePlayer.motionX) + java.lang.Math.abs(Client.mc.thePlayer.motionZ) < Client.mc.thePlayer.capabilities.getWalkSpeed() / 3 && Client.mc.thePlayer.posY + 1 >= wayPoints.get(currentPoint).getY());

        if (Client.mc.thePlayer.getDistanceSq(wayPoints.get(currentPoint).add(0, 1, 0)) < 1 + java.lang.Math.abs(Client.mc.thePlayer.posY - wayPoints.get(currentPoint).add(0, 1, 0).getY())) {
            currentPoint++;
        }
    }

    public Walker(List<BlockPos> wayPoints, Runnable callback) {
        this.wayPoints = wayPoints;
        this.callback = callback;
    }
}