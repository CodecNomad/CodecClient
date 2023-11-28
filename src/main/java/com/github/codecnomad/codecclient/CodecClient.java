package com.github.codecnomad.codecclient;

import com.github.codecnomad.codecclient.guis.GuiConfig;
import com.github.codecnomad.codecclient.classes.HelperClassModule;
import com.github.codecnomad.codecclient.classes.HelperClassRotation;
import com.github.codecnomad.codecclient.modules.MacroFishing;
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
    public static Map<String, HelperClassModule> modules = new HashMap<>();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static HelperClassRotation helperClassRotation = new HelperClassRotation();
    public static GuiConfig guiConfig;

    static {
        modules.put("FishingMacro", new MacroFishing());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        guiConfig = new GuiConfig();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(helperClassRotation);
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        for (Map.Entry<String, HelperClassModule> moduleMap : modules.entrySet()) {
            moduleMap.getValue().unregister();
        }
    }
}
