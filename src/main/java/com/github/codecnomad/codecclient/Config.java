package com.github.codecnomad.codecclient;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;

import java.io.File;

public class Config extends Vigilant {
    public static boolean state = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "EntityEsp",
            category = "Modules"
    )
    private boolean EntityEsp = CodecClient.modules.get("EntityEsp").state;

    @Property(
            type = PropertyType.TEXT,
            name = "Entities",
            description = "These are based on the mob names, Use: Item1;Item2",
            category = "Esp"
    )
    public static String EntityEspWhitelist = "Armor Stand;Anita".toLowerCase();

    @Property(
            type = PropertyType.SWITCH,
            name = "BlockEsp",
            category = "Modules"
    )
    private boolean BlockEsp = CodecClient.modules.get("BlockEsp").state;

    @Property(
            type = PropertyType.TEXT,
            name = "Blocks",
            description = "These are based on the block names, Use: Item1;Item2",
            category = "Esp"
    )
    public static String BlockEspWhitelist = "Coal;Iron".toLowerCase();

    @Property(
            type = PropertyType.SLIDER,
            name = "Block radius",
            description = "The higher, the less fps you'll have",
            category = "Esp",
            min = 5,
            max = 150
    )
    public static int BlockEspRadius = 10;

    @Property(
            type = PropertyType.SLIDER,
            name = "Block update",
            description = "The higher, the more fps you'll have, information may be outdated at high values",
            category = "Esp",
            min = 0,
            max = 200
    )
    public static int BlockUpdate = 60;

    public Config() {
        super(new File("./config/CodecClient.toml"));

        this.registerListener("EntityEsp", newState -> {
            toggle("EntityEsp", (Boolean) newState);
        });
        this.registerListener("BlockEsp", newState -> {
            toggle("BlockEsp", (Boolean) newState);
        });
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
