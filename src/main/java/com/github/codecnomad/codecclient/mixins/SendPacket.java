package com.github.codecnomad.codecclient.mixins;

import com.github.codecnomad.codecclient.classes.PacketEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class SendPacket {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void packetReceive(Packet packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.SendEvent(packetIn));
    }
}
