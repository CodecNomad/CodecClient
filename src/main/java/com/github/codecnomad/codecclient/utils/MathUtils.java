package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.util.BlockPos;

public class MathUtils {

    public static float easeInOut(float t) {
        return t * t * (3.0f - 2.0f * t);
    }
    public static float interpolate(float goal, float current, float time) {
        float t = easeInOut(time);
        return current + (goal - current) * t;
    }

    float getYaw(BlockPos pos) {
        return (float) Math.toDegrees(Math.atan2(pos.getZ(), pos.getX()));
    }

    float getPitch(BlockPos pos) {
        return (float) Math.toDegrees(-Math.asin(pos.getY() / CodecClient.mc.thePlayer.getDistanceSq(pos)));
    }
}
