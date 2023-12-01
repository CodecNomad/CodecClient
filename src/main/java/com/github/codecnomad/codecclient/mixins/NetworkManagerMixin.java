package com.github.codecnomad.codecclient.mixins;

import com.github.codecnomad.codecclient.events.PacketEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Unique
    private EventBus codecClient$eventBus = MinecraftForge.EVENT_BUS;

    @Shadow
    @Final
    private EnumPacketDirection direction;

    @Shadow private Channel channel;

    @Inject(
            method = "dispatchPacket",
            at = @At("HEAD")
    )
    private void preDispatchPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>>[] futureListeners, CallbackInfo ci) {
        if (this.direction != EnumPacketDirection.CLIENTBOUND) {
            return;
        }

        codecClient$eventBus.post(new PacketEvent.SendEvent(packet, PacketEvent.Phase.pre));
    }

    @Inject(
            method = "dispatchPacket",
            at = @At("RETURN")
    )
    private void postDispatchPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>>[] futureListeners, CallbackInfo ci) {
        if (this.direction != EnumPacketDirection.CLIENTBOUND) {
            return;
        }

        codecClient$eventBus.post(new PacketEvent.SendEvent(packet, PacketEvent.Phase.post));
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/network/Packet.processPacket(Lnet/minecraft/network/INetHandler;)V"
            )
    )
    private void preProcessPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (this.direction != EnumPacketDirection.CLIENTBOUND) {
            return;
        }

        codecClient$eventBus.post(new PacketEvent.ReceiveEvent(packet, PacketEvent.Phase.pre));
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
            at = @At("RETURN")
    )
    private void postProcessPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (!this.channel.isOpen() || this.direction != EnumPacketDirection.CLIENTBOUND) {
            return;
        }

        codecClient$eventBus.post(new PacketEvent.ReceiveEvent(packet, PacketEvent.Phase.post));
    }
}