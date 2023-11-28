package com.github.codecnomad.codecclient.mixins;

import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(S19PacketEntityHeadLook.class)
public interface AccessorS19 {
    @Accessor("entityId")
    int getEntityId();
}
