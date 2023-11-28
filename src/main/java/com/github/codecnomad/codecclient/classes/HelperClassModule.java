package com.github.codecnomad.codecclient.classes;

import net.minecraftforge.common.MinecraftForge;

public class HelperClassModule {
    public boolean state;

    public HelperClassModule() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        this.state = true;
    }

    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.state = false;
    }
}
