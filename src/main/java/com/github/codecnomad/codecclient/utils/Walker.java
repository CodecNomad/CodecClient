package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
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

        float yawDifference = Math.getYaw(wayPoints.get(currentPoint)) - Client.mc.thePlayer.cameraYaw;
        Chat.sendMessage(String.valueOf(yawDifference));

        if (yawDifference > -45 && yawDifference <= 45) {
            movementHelper(true, false, false, false);
        }

        if (yawDifference > -135 && yawDifference <= -45) {
            movementHelper(false, true, false, false);
        }

        if (yawDifference > 45 && yawDifference <= 135) {
            movementHelper(false, false, true, false);
        }

        if (yawDifference > -180 && yawDifference <= -135 || yawDifference > 135 && yawDifference <= 180) {
            movementHelper(false, false, false, true);
        }


        if (Client.mc.thePlayer.getDistanceSq(wayPoints.get(currentPoint).add(0, 1, 0)) < 1) {
            currentPoint++;
        }
    }

    public void movementHelper(boolean f, boolean l, boolean r, boolean b) {
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindForward.getKeyCode(), f);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindLeft.getKeyCode(), l);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindRight.getKeyCode(), r);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindBack.getKeyCode(), b);
    }

    public Walker(List<BlockPos> wayPoints, Runnable callback) {
        this.wayPoints = wayPoints;
        this.callback = callback;
    }
}
