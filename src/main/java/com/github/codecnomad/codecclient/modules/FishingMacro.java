package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.classes.Module;
import com.github.codecnomad.codecclient.classes.PacketEvent;
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
    private static final int LOOK_AROUND = 1;
    private static final int CASTING_HOOK = 2;
    private static final int HOOK_CHECK_STATE = 3;

    private static final int SET = 0;
    private static final int RESET = 1;
    private float yawToReturnTo;
    private float pitchToReturnTo;

    private int currentState = FIND_ROD;
    private boolean failSafe = false;

    private int cycle = 0;
    private int counter = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (failSafe) {
            return;
        }

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
                        currentState = LOOK_AROUND;
                        return;
                    }
                }

                ChatUtils.sendMessage("Disabled macro -> couldn't find rod.");
                this.unregister();

                return;
            }

            case LOOK_AROUND: {
                if (CodecClient.mc.thePlayer == null || counter < 20) {
                    counter++;
                    return;
                } counter = 0;

                switch (cycle) {
                    case SET: {
                        yawToReturnTo = (int) (-15 + Math.random() * 30);
                        pitchToReturnTo = (int) (-15 + Math.random() * 30);
                        CodecClient.rotation.setYaw(CodecClient.mc.thePlayer.rotationYaw + yawToReturnTo, 10);
                        CodecClient.rotation.setPitch(CodecClient.mc.thePlayer.rotationPitch + pitchToReturnTo, 10);

                        currentState = CASTING_HOOK;
                        cycle = RESET;
                        break;
                    }

                    case RESET: {
                        CodecClient.rotation.setYaw(CodecClient.mc.thePlayer.rotationYaw - yawToReturnTo, 10);
                        CodecClient.rotation.setPitch(CodecClient.mc.thePlayer.rotationPitch - pitchToReturnTo, 10);

                        currentState = HOOK_CHECK_STATE;
                        cycle = SET;
                        break;
                    }
                }
            }

            case CASTING_HOOK: {
                if (CodecClient.mc.thePlayer == null || counter < 20) {
                    counter++;
                    return;
                } counter = 0;

                CodecClient.mc.playerController.sendUseItem(CodecClient.mc.thePlayer, CodecClient.mc.thePlayer.getEntityWorld(), CodecClient.mc.thePlayer.inventory.getCurrentItem());
                currentState = LOOK_AROUND;

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
        if (event.packet instanceof S18PacketEntityTeleport) {
            if (((S18PacketEntityTeleport) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()) {
                CodecClient.rotation.reset();
                failSafe = true;
            }
        }
    }

    int fCounter = 0;
    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (failSafe) {
            fCounter++;
        }

        if (fCounter == 20) {
            ChatUtils.sendMessage("Disabled macro -> failsafe has been triggered");
            CodecClient.mc.thePlayer.sendChatMessage("uhm hello?");
            CodecClient.rotation.setYaw(CodecClient.mc.thePlayer.rotationYaw + (int) (-45 + Math.random() * 45), 10);
        }

        if (fCounter == 80) {
            CodecClient.mc.thePlayer.sendChatMessage("tf was that");
            CodecClient.rotation.setYaw(CodecClient.mc.thePlayer.rotationYaw + (int) (-45 + Math.random() * 45), 10);
            failSafe = false;
            fCounter = 0;
            this.unregister();
        }
    }
}
