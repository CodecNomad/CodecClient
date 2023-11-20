package com.github.codecnomad.codecclient.classes;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.utils.ChatUtils;
import com.github.codecnomad.codecclient.utils.MathUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Rotation {
    private float yawGoal = 0;
    private float pitchGoal = 0;

    public boolean updateYaw = false;
    public boolean updatePitch = false;

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
