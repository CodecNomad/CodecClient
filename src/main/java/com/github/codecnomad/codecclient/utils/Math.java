package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.util.BlockPos;

public class Math {

    public static float easeInOut(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static float interpolate(float goal, float current, float time) {
        while (goal - current > 180) {
            current += 360;
        }
        while (goal - current < -180) {
            current -= 360;
        }

        float t = easeInOut(time);
        return current + (goal - current) * t;
    }

    public static String toClock(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%dh, %dm, %ds", hours, minutes, remainingSeconds);
    }

    public static String toFancyNumber(int number) {
        int k = number / 1000;
        int m = number / 1000000;
        int remaining = number % 1000000;

        if (m > 0) {
            return String.format("%dM", m);
        } else if (k > 0) {
            return String.format("%dk", k);
        } else {
            return String.format("%d", remaining);
        }
    }

    public static float getYaw(BlockPos blockPos) {
        double deltaX = blockPos.getX() + 0.5 - Client.mc.thePlayer.posX;
        double deltaZ = blockPos.getZ() + 0.5 - Client.mc.thePlayer.posZ;
        double yawToBlock = java.lang.Math.atan2(-deltaX, deltaZ);
        double yaw = java.lang.Math.toDegrees(yawToBlock);

        yaw = (yaw + 360) % 360;
        if (yaw > 180) {
            yaw -= 360;
        }

        return (float) yaw;
    }


    public static float getPitch(BlockPos blockPos) {
        double deltaX = blockPos.getX() + 0.5 - Client.mc.thePlayer.posX;
        double deltaY = blockPos.getY() + 0.5 - Client.mc.thePlayer.posY - Client.mc.thePlayer.getEyeHeight();
        double deltaZ = blockPos.getZ() + 0.5 - Client.mc.thePlayer.posZ;
        double distanceXZ = java.lang.Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double pitchToBlock = -java.lang.Math.atan2(deltaY, distanceXZ);
        double pitch = java.lang.Math.toDegrees(pitchToBlock);
        pitch = java.lang.Math.max(-90, java.lang.Math.min(90, pitch));

        return (float) pitch;
    }


}