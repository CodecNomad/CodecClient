package com.github.codecnomad.codecclient.classes;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.utils.ChatUtils;
import com.github.codecnomad.codecclient.utils.MathUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Rotation {
    private float yawGoal = 0;
    private float pitchGoal = 0;

    private boolean updateYaw = false;
    private boolean updatePitch = false;

    private int yawSmooth;
    private int pitchSmooth;

    public void setYaw(float yaw, int time) {
        yawGoal = yaw;
        yawSmooth = time;
        updateYaw = true;
    }

    public void setPitch(float pitch, int time) {
        pitchGoal = pitch;
        pitchSmooth = time;
        updatePitch = true;
    }

    public void reset() {
        updateYaw = false;
        updatePitch = false;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.PlayerTickEvent event) {
        if (updateYaw) {
            CodecClient.mc.thePlayer.rotationYaw = MathUtils.interpolate(yawGoal, CodecClient.mc.thePlayer.rotationYaw, (float) 1 / yawSmooth);

            if (Math.abs(CodecClient.mc.thePlayer.rotationYaw - yawGoal) < 3f) {
                updateYaw = false;
            }
        }

        if (updatePitch) {
            CodecClient.mc.thePlayer.rotationPitch = MathUtils.interpolate(pitchGoal, CodecClient.mc.thePlayer.rotationPitch, (float) 1 / pitchSmooth);

            if (Math.abs(CodecClient.mc.thePlayer.rotationPitch - pitchGoal) < 3f) {
                updatePitch = false;
            }
        }
    }
}
