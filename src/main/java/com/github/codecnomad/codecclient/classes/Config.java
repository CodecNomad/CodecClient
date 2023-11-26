package com.github.codecnomad.codecclient.classes;

import cc.polyfrost.oneconfig.config.annotations.KeyBind;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import com.github.codecnomad.codecclient.CodecClient;
import org.lwjgl.input.Keyboard;

public class Config extends cc.polyfrost.oneconfig.config.Config {
    @KeyBind(
            name = "Fishing keybind",
            category = "Macros",
            subcategory = "Fishing"
    )
    public static OneKeyBind FishingKeybinding = new OneKeyBind(Keyboard.KEY_F);

    @Number(
            name = "Catch delay",
            category = "Macros",
            subcategory = "Fishing",
            min = 1,
            max = 10
    )
    public static int FishingDelay = 10;

    @Number(
            name = "Kill delay",
            category = "Macros",
            subcategory = "Fishing",
            min = 1,
            max = 40
    )
    public static int KillDelay = 20;

    @Number(
            name = "Attack c/s",
            category = "Macros",
            subcategory = "Fishing",
            min = 5,
            max = 20
    )
    public static int AttackCps = 10;

    public Config() {
        super(new Mod("codecclient", ModType.UTIL_QOL), "config.json");
        this.registerKeyBind(FishingKeybinding, () -> toggle("FishingMacro"));
        initialize();
    }

    private static void toggle(String name) {
        Module module = CodecClient.modules.get(name);
        if (module.state) {
            module.unregister();
        } else {
            module.register();
        }
    }
}
