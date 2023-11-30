package com.github.codecnomad.codecclient.guis;

import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.classes.HelperClassModule;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class GuiConfig extends cc.polyfrost.oneconfig.config.Config {
    @Color(
            name = "Color",
            category = "Visuals"
    )
    public static OneColor VisualColor = new OneColor(100, 60, 160, 200);
    @KeyBind(
            name = "Fishing key-bind",
            category = "Macros",
            subcategory = "Fishing"
    )
    public static OneKeyBind FishingKeybinding = new OneKeyBind(Keyboard.KEY_F);
    @Number(
            name = "Catch delay",
            category = "Macros",
            subcategory = "Fishing",
            min = 1,
            max = 20
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

    @Number(
            name = "Smoothing",
            category = "Macros",
            subcategory = "Fishing",
            min = 2,
            max = 10
    )
    public static int RotationSmoothing = 4;

    @Switch(
            name = "Auto kill",
            category = "Macros",
            subcategory = "Fishing"
    )
    public static boolean AutoKill = true;

    @Switch(
            name = "Only sound failsafe",
            category = "Macros",
            subcategory = "Fishing"
    )
    public static boolean OnlySound = false;

    @HUD(
            name = "Fishing HUD",
            category = "Visuals"
    )
    public HudFishing hudFishing = new HudFishing();

    public GuiConfig() {
        super(new Mod("codecclient", ModType.UTIL_QOL), "config.json");
        this.registerKeyBind(FishingKeybinding, () -> toggle("FishingMacro"));
        initialize();
    }

    private static void toggle(String name) {
        HelperClassModule helperClassModule = CodecClient.modules.get(name);
        if (helperClassModule.state) {
            helperClassModule.unregister();
        } else {
            helperClassModule.register();
        }
    }
}
