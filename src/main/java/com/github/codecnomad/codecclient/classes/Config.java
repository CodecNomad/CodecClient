package com.github.codecnomad.codecclient.classes;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.classes.Module;
import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;

import java.io.File;

public class Config extends Vigilant {
    public static boolean state = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "EntityEsp",
            category = "EntityEsp",
            subcategory = "Config"
    )
    private boolean EntityEsp = CodecClient.modules.get("EntityEsp").state;

    @Property(
            type = PropertyType.SWITCH,
            name = "FishingMacro",
            category = "FishingMacro",
            subcategory = "Config"
    )
    private boolean FishingMacro = CodecClient.modules.get("FishingMacro").state;

    @Property(
            type = PropertyType.TEXT,
            name = "Entities",
            description = "These are based on the mob names, Use: Item1;Item2",
            category = "EntityEsp",
            subcategory = "Config"
    )
    public static String EntityEspWhitelist = "Armor Stand;Anita";

    @Property(
            type = PropertyType.DECIMAL_SLIDER,
            minF = 0,
            maxF = 360,
            name = "Hue",
            category = "All Esp",
            subcategory = "Looks/Color"
    )
    public static float EntityEspColorH = 200;
    @Property(
            type = PropertyType.DECIMAL_SLIDER,
            minF = 0,
            maxF = 360,
            name = "Saturation",
            category = "All Esp",
            subcategory = "Looks/Color"
    )
    public static float EntityEspColorS = 80;
    @Property(
            type = PropertyType.DECIMAL_SLIDER,
            minF = 0,
            maxF = 360,
            name = "Brightness",
            category = "All Esp",
            subcategory = "Looks/Color"
    )
    public static float EntityEspColorB = 100;
    @Property(
            type = PropertyType.DECIMAL_SLIDER,
            minF = 0.1f,
            maxF= 1.f,
            name = "Width",
            category = "EntityEsp",
            subcategory = "Looks/Size"
    )
    public static float EntityEspWidth = 0.5f;

    public Config() {
        super(new File("./CodecClient.toml"));

        this.registerListener("EntityEsp", newState -> toggle("EntityEsp", (Boolean) newState));
        this.registerListener("FishingMacro", newState -> toggle("FishingMacro", (Boolean) newState));

    }

    private void toggle(String name, Boolean state) {
        Module module = CodecClient.modules.get(name);
        if (state) {
            module.register();
        } else {
            module.unregister();
        }
    }
}
