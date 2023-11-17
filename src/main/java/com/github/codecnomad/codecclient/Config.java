package com.github.codecnomad.codecclient;

import com.github.codecnomad.codecclient.modules.BlockEsp;
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
    public static String BlockEspWhitelist = "iron_block;coal_block".toLowerCase();

    @Property(
            type = PropertyType.SLIDER,
            name = "Block radius",
            description = "Set this to the size of the world, where you use your esp",
            category = "Esp",
            min = 5,
            max = 150
    )
    public static int BlockEspRadius = 100;

    @Property(
            type = PropertyType.BUTTON,
            name = "Update block esp",
            description = "Press me after changing any settings to take effect",
            category = "Esp"
    )
    private void UpdateBlockEsp() {
        com.github.codecnomad.codecclient.modules.BlockEsp.shouldUpdateBlocks = true;
    }

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
