package com.github.codecnomad.codecclient.mixins;

import com.github.codecnomad.codecclient.PacketEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class ReceivePacket {
    @Inject(method = "handleEntityHeadLook", at = @At("HEAD"))
    public void packetReceive(S19PacketEntityHeadLook packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packetIn));
    }

    @Inject(method = "handleEntityTeleport", at = @At("HEAD"))
    public void packetReceive(S18PacketEntityTeleport packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packetIn));
    }

    @Inject(method = "handleEntityVelocity", at = @At("HEAD"))
    public void packetReceive(S12PacketEntityVelocity packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packetIn));
    }

    @Inject(method = "handleHeldItemChange", at = @At("HEAD"))
    public void packetReceive(S09PacketHeldItemChange packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packetIn));
    }

    @Inject(method = "handlePlayerPosLook", at = @At("HEAD"))
    public void packetReceive(S08PacketPlayerPosLook packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packetIn));
    }

    @Inject(method = "handleEntityAttach", at = @At("HEAD"))
    public void packetReceive(S1BPacketEntityAttach packetIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packetIn));
    }
}
