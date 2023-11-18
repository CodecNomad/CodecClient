package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.util.BlockPos;

public class MathUtils {
    float interpolate(float goal, float current, float time) {
        float difference = goal - current;

        if (difference > time) {
            return current + time;
        }

        if (difference < time) {
            return current - time;
        }

        return goal;
    }

    float getYaw(BlockPos pos) {
        return (float) Math.toDegrees(Math.atan2(pos.getZ(), pos.getX()));
    }

    float getPitch(BlockPos pos) {
        return (float) Math.toDegrees(-Math.asin(pos.getY() / CodecClient.mc.thePlayer.getDistanceSq(pos)));
    }
}
