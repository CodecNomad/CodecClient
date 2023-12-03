package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Rotation {
    public boolean updateYaw = false;
    public boolean updatePitch = false;
    private float yawGoal = 0;
    private float pitchGoal = 0;
    private int yawSmooth;
    private int pitchSmooth;

    public void setYaw(float yaw, int smoothing) {
        yawGoal = yaw;
        yawSmooth = smoothing;
        updateYaw = true;
    }

    public void setPitch(float pitch, int smoothing) {
        pitchGoal = pitch;
        pitchSmooth = smoothing;
        updatePitch = true;
    }

    public void reset() {
        updateYaw = false;
        updatePitch = false;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (updateYaw) {
            Client.mc.thePlayer.rotationYaw = Math.interpolate(yawGoal, Client.mc.thePlayer.rotationYaw, (float) 1 / yawSmooth);
            if (java.lang.Math.abs(Client.mc.thePlayer.rotationYaw - yawGoal) < java.lang.Math.random() * 3) {
                updateYaw = false;
            }
        }

        if (updatePitch) {
            Client.mc.thePlayer.rotationPitch = Math.interpolate(pitchGoal, Client.mc.thePlayer.rotationPitch, (float) 1 / pitchSmooth);
            if (java.lang.Math.abs(Client.mc.thePlayer.rotationPitch - pitchGoal) < java.lang.Math.random() * 3) {
                updatePitch = false;
            }
        }
    }
}
