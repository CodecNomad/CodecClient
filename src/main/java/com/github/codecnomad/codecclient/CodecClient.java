package com.github.codecnomad.codecclient;

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
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Mod(modid = "codecclient", useMetadata=true)
public class CodecClient {
    public static Logger logger = Logger.getLogger("CodecClient");

    public static Map<String, Module> modules = new HashMap<>();
    static {
        modules.put("EntityEsp", new EntityEsp());
        modules.put("FishingMacro", new FishingMacro());
    }

    public static Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new Command());
    }

    @SubscribeEvent public void tick(TickEvent.RenderTickEvent event) {
        if (Config.state) {
            Config.state = false;
            mc.displayGuiScreen(new Config().gui());
        }
    }

    @SubscribeEvent public void input(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
            ChatUtils.sendMessage("Turned off all macros.");
            for (Map.Entry<String, Module> moduleMap : modules.entrySet()) {
                moduleMap.getValue().unregister();
            }
        }
    }
}
