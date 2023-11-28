package com.github.codecnomad.codecclient.classes;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

public class HelperClassPacketEvent extends Event {
    public final Packet<?> packet;

    public HelperClassPacketEvent(final Packet<?> packet) {
        this.packet = packet;
    }

    public static class ReceiveEventHelperClass extends HelperClassPacketEvent {
        public ReceiveEventHelperClass(final Packet<?> packet) {
            super(packet);
        }
    }

    public static class SendEventHelperClass extends HelperClassPacketEvent {
        public SendEventHelperClass(final Packet<?> packet) {
            super(packet);
        }
    }
}