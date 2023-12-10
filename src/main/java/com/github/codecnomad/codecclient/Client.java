package com.github.codecnomad.codecclient;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.github.codecnomad.codecclient.command.MainCommand;
import com.github.codecnomad.codecclient.modules.FishingMacro;
import com.github.codecnomad.codecclient.modules.Module;
import com.github.codecnomad.codecclient.ui.Config;
import com.github.codecnomad.codecclient.utils.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = "codecclient", useMetadata = true)
public class Client {
    public static Map<String, Module> modules = new HashMap<>();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Rotation rotation = new Rotation();
    public static Config guiConfig;

    static {
        modules.put("FishingMacro", new FishingMacro());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        guiConfig = new Config();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(rotation);

        MinecraftForge.EVENT_BUS.register(MainCommand.pathfinding);

        CommandManager.register(new MainCommand());

        // fishing HUD

        try {
            FileWriter file = new FileWriter("main/resources/fishingHUD.json");
            file.write("{\n" +
                            "  \"startTime\":0,\n" +
                            "  \"elapsedTimeSeconds\": 0,\n" +
                            "  \"averageCPH\": 0,\n" +
                            "  \"averageXPH\":0\n" +
                            "}");
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        for (Map.Entry<String, Module> moduleMap : modules.entrySet()) {
            moduleMap.getValue().unregister();
        }
    }
}
