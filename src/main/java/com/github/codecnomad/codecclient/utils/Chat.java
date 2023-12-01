package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.util.ChatComponentText;

public class Chat {
    public static void sendMessage(String message) {
        Client.mc.thePlayer.addChatMessage(new ChatComponentText(String.format("§c§lCodecClient >> §7 %s", message)));
    }
}
