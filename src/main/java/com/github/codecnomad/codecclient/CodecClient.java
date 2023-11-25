package com.github.codecnomad.codecclient;

import com.github.codecnomad.codecclient.classes.Command;
import com.github.codecnomad.codecclient.classes.Config;
import com.github.codecnomad.codecclient.classes.Module;
import com.github.codecnomad.codecclient.classes.Rotation;
import com.github.codecnomad.codecclient.modules.EntityEsp;
import com.github.codecnomad.codecclient.modules.FishingMacro;
import com.github.codecnomad.codecclient.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

@Mod(modid = "codecclient", useMetadata = true)
public class CodecClient {
    public static Map<String, Module> modules = new HashMap<>();
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Rotation rotation = new Rotation();

    static {
        modules.put("EntityEsp", new EntityEsp());
        modules.put("FishingMacro", new FishingMacro());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(rotation);
        ClientCommandHandler.instance.registerCommand(new Command());
    }

    @SubscribeEvent
    public void tick(TickEvent.RenderTickEvent event) {
        if (Config.state) {
            Config.state = false;
            mc.displayGuiScreen(new Config().gui());
        }
    }

    @SubscribeEvent
    public void input(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
            ChatUtils.sendMessage("Turned off all macros.");
            for (Map.Entry<String, Module> moduleMap : modules.entrySet()) {
                moduleMap.getValue().unregister();
            }
        }
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        for (Map.Entry<String, Module> moduleMap : modules.entrySet()) {
            moduleMap.getValue().unregister();
        }
    }
}
