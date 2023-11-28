package com.github.codecnomad.codecclient.mixins;

import com.github.codecnomad.codecclient.classes.HelperClassPacketEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("rawtypes")
@Mixin(NetworkManager.class)
public class MixinSendPacket {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    public void packetReceive(Packet packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new HelperClassPacketEvent.SendEventHelperClass(packetIn));
    }
}
