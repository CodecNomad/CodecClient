package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.util.ChatComponentText;

public class ChatUtils {
    public static void sendMessage(String message) {
        CodecClient.mc.thePlayer.addChatMessage(new ChatComponentText("§cCodecClient >> §7 " + message));
    }
}
