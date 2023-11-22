package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.util.BlockPos;

public class MathUtils {

    public static float humanEaseInOut(float progress) {
        float easedProgress = progress * progress * (3.0f - 2.0f * progress);

        float breathing = 0.1f * (float)Math.sin(progress * Math.PI);
        float humanizedProgress = easedProgress + breathing;

        return Math.min(1.0f, Math.max(0.0f, humanizedProgress));
    }
    public static float interpolate(float goal, float current, float time) {
        float t = humanEaseInOut(time);
        return current + (goal - current) * t;
    }

    public static float getYaw(BlockPos p) {
        return (float) Math.toDegrees(Math.atan2(p.getZ(), p.getX()));
    }

    public static float getPitch(BlockPos p) {
        return (float) Math.toDegrees(-Math.asin(p.getY() / CodecClient.mc.thePlayer.getDistanceSq(p)));
    }
}
