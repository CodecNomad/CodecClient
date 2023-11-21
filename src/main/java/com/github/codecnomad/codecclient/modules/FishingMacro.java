package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.classes.Module;
import com.github.codecnomad.codecclient.classes.PacketEvent;
import com.github.codecnomad.codecclient.mixins.S19Accessor;
import com.github.codecnomad.codecclient.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class FishingMacro extends Module {
    private static final String[] FAILSAFE_TEXT = new String[] {"?", "you good?", "HI IM HERE", "can you not bro", "can you dont", "hiiiiii", "can i get friend request??", "henlo i'm here",};
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
        if (
                event.packet instanceof S08PacketPlayerPosLook ||
                event.packet instanceof S09PacketHeldItemChange ||
                (event.packet instanceof S19PacketEntityHeadLook && ((S19Accessor) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()) ||
                (event.packet instanceof S1BPacketEntityAttach && ((S1BPacketEntityAttach) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()) ||
                (event.packet instanceof S18PacketEntityTeleport && ((S18PacketEntityTeleport) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId())
        ) {
            CodecClient.rotation.reset();
            failSafe = true;
        }
    }

    int fCounter = 0;
    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (!failSafe) {
            return;
        }
        fCounter++;

        CodecClient.mc.thePlayer.playSound("random.anvil_land", 10.f, 1.f);

        if (fCounter == 20) {
            ChatUtils.sendMessage("Disabled macro -> failsafe has been triggered");
            CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw -89 + (Math.random() * 180)), 7);
            CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch -14 + (Math.random() * 30)), 7);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
        }

        if (fCounter == 45) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
        }

        if (fCounter == 60) {
            CodecClient.mc.thePlayer.sendChatMessage(FAILSAFE_TEXT[(int) (Math.random() * FAILSAFE_TEXT.length)]);
            CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw -89 + (Math.random() * 180)), 7);
            CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch -14 + (Math.random() * 30)), 7);
        }

        if (fCounter == 80) {
            CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw -89 + (Math.random() * 180)), 7);
            CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch -14 + (Math.random() * 30)), 7);
        }

        if (fCounter == 90) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
        }

        if (fCounter == 105) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
        }

        if (fCounter == 120) {
            failSafe = false;
            fCounter = 0;
            this.unregister();
        }
    }
}
