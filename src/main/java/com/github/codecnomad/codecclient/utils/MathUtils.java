package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.util.BlockPos;

public class MathUtils {

    public static float easeInOut(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static float interpolate(float goal, float current, float time) {
        float t = easeInOut(time);
        return current + (goal - current) * t;
    }

    public static float getYaw(BlockPos blockPos) {
        double deltaX = blockPos.getX() + 0.5 - CodecClient.mc.thePlayer.posX;
        double deltaZ = blockPos.getZ() + 0.5 - CodecClient.mc.thePlayer.posZ;
        double yawToBlock = Math.atan2(-deltaX, deltaZ);
        double yaw = Math.toDegrees(yawToBlock);

        return (float) yaw;
    }

    public static float getPitch(BlockPos blockPos) {
        double deltaX = blockPos.getX() + 0.5 - CodecClient.mc.thePlayer.posX;
        double deltaY = blockPos.getY() + 0.5 - CodecClient.mc.thePlayer.posY - CodecClient.mc.thePlayer.getEyeHeight();
        double deltaZ = blockPos.getZ() + 0.5 - CodecClient.mc.thePlayer.posZ;
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double pitchToBlock = -Math.atan2(deltaY, distanceXZ);
        double pitch = Math.toDegrees(pitchToBlock);

        return (float) pitch;
    }


}