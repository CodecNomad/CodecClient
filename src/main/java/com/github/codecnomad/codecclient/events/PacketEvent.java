package com.github.codecnomad.codecclient.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PacketEvent extends Event {
    public enum Phase {
        pre,
        post
    }

    public final Packet<?> packet;
    public final Phase phase;


    public PacketEvent(final Packet<?> packet, Phase phase) {
        this.packet = packet;
        this.phase = phase;
    }

    public static class ReceiveEvent extends PacketEvent {
        public ReceiveEvent(final Packet<?> packet, Phase phase) {
            super(packet, phase);
        }
    }

    public static class SendEvent extends PacketEvent {
        public SendEvent(final Packet<?> packet, Phase phase) {
            super(packet, phase);
        }
    }
}