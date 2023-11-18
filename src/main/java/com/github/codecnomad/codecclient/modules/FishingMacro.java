package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.Module;
import com.github.codecnomad.codecclient.PacketEvent;
import com.github.codecnomad.codecclient.mixins.S19Accessor;
import com.github.codecnomad.codecclient.utils.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FishingMacro extends Module {
    private static final int FIND_ROD = 0;
    private static final int CASTING_HOOK = 1;
    private static final int HOOK_CHECK_STATE = 2;

    private int currentState = FIND_ROD;
    private int counter = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        switch (currentState) {
            case FIND_ROD: {
                if (CodecClient.mc.thePlayer == null || counter < 20) {
                    counter++;
                    return;
                } counter = 0;

                for (int slotIndex = 0; slotIndex < CodecClient.mc.thePlayer.inventory.getSizeInventory(); slotIndex++) {
                    ItemStack stack = CodecClient.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                        CodecClient.mc.thePlayer.inventory.currentItem = slotIndex;
                        currentState = CASTING_HOOK;
                        return;
                    }
                }

                ChatUtils.sendMessage("Disabled macro -> couldn't find rod.");
                this.unregister();

                return;
            }

            case CASTING_HOOK: {
                if (CodecClient.mc.thePlayer == null || counter < 20) {
                    counter++;
                    return;
                } counter = 0;

                CodecClient.mc.playerController.sendUseItem(CodecClient.mc.thePlayer, CodecClient.mc.thePlayer.getEntityWorld(), CodecClient.mc.thePlayer.inventory.getCurrentItem());
                currentState = HOOK_CHECK_STATE;

                return;
            }

            case HOOK_CHECK_STATE:
            {
                if (CodecClient.mc.thePlayer == null || counter < 20) {
                    counter++;
                    return;
                } counter = 0;

                Entity fishingHook = null;
                for (Entity entity : CodecClient.mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityFishHook && ((EntityFishHook) entity).angler == CodecClient.mc.thePlayer) {
                        fishingHook = entity;
                    }
                }

                if (fishingHook == null) {
                    currentState = FIND_ROD;
                    return;
                }

                if (fishingMarker != null && fishingMarker.isEntityAlive() && fishingMarker.getName().contains("!!!")) {
                    CodecClient.mc.playerController.sendUseItem(CodecClient.mc.thePlayer, CodecClient.mc.thePlayer.getEntityWorld(), CodecClient.mc.thePlayer.inventory.getCurrentItem());

                    currentState = FIND_ROD;
                    fishingMarker = null;
                }

                return;
            }
        }
    }

    Entity fishingMarker;
    @SubscribeEvent
    public void entitySpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityArmorStand) {
            fishingMarker = event.entity;
        }
    }

    @SubscribeEvent
    public void packetReceive(PacketEvent.ReceiveEvent event) {
        if (event.packet instanceof S19PacketEntityHeadLook) {
            if (((S19Accessor) event.packet).getEntityId() != CodecClient.mc.thePlayer.getEntityId()) {
                return;
            }
        }

        else if (event.packet instanceof S18PacketEntityTeleport) {
            if (((S18PacketEntityTeleport) event.packet).getEntityId() != CodecClient.mc.thePlayer.getEntityId()) {
                return;
            }
        }

        else if (event.packet instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.packet).getEntityID() != CodecClient.mc.thePlayer.getEntityId()) {
                return;
            }
        }

        else if (event.packet instanceof S1BPacketEntityAttach) {
            if (((S1BPacketEntityAttach) event.packet).getEntityId() != CodecClient.mc.thePlayer.getEntityId()) {
                return;
            }
        }

        ChatUtils.sendMessage("Disabled macro -> failsafe has been triggered");
        this.unregister();
    }
}
