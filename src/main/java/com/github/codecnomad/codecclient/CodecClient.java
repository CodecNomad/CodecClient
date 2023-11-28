package com.github.codecnomad.codecclient;

import com.github.codecnomad.codecclient.Guis.Config;
import com.github.codecnomad.codecclient.classes.Module;
import com.github.codecnomad.codecclient.classes.Rotation;
import com.github.codecnomad.codecclient.modules.FishingMacro;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = "codecclient", useMetadata = true)
public class CodecClient {
    public static Map<String, Module> modules = new HashMap<>();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Rotation rotation = new Rotation();
    public static Config config;

    static {
        modules.put("FishingMacro", new FishingMacro());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config = new Config();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(rotation);
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        for (Map.Entry<String, Module> moduleMap : modules.entrySet()) {
            moduleMap.getValue().unregister();
        }
    }
}
