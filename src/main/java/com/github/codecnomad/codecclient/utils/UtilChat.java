package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.util.ChatComponentText;

import java.util.regex.Pattern;

public class UtilChat {
    public static final Pattern FishingSkillPattern = Pattern.compile("\\+([\\d.,]+) Fishing", Pattern.CASE_INSENSITIVE);

    public static void sendMessage(String message) {
        CodecClient.mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§c§lCodecClient >> §7 %s", message)));
    }
}
