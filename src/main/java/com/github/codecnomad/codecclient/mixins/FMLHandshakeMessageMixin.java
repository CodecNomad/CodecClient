package com.github.codecnomad.codecclient.mixins;

import com.github.codecnomad.codecclient.Client;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@SuppressWarnings("rawtypes")
@Mixin(value = FMLHandshakeMessage.ModList.class, remap = false)
public class FMLHandshakeMessageMixin {
    @Shadow
    private Map<String, String> modTags;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At(value = "RETURN"))
    public void test(java.util.List modList, CallbackInfo ci) {
        if (Client.mc.isSingleplayer()) return;
        this.modTags.keySet().removeIf(c -> !c.matches("FML|Forge|mcp"));
    }
}
