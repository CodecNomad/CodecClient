package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.classes.Counter;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FishingMacro extends Module {

    public static final String[] FAILSAFE_TEXT = new String[] {"?", "you good?", "HI IM HERE", "can you not bro", "can you dont", "hiiiiii", "can i get friend request??", "henlo i'm here",};

    public enum FishingSteps {
        FIND_ROD,
        LOOK_TO_NEW_BLOCK,
        CAST_HOOK,
        WAIT_FOR_CATCH,
        CATCH
    };
    public FishingSteps currentStep = FishingSteps.FIND_ROD;

    public Counter MainCounter = new Counter();
    public Counter FailsafeCounter = new Counter();
    public boolean failSafe = false;

    Entity fishingHook = null;
    Entity fishingMarker = null;

    @Override
    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.state = false;

        currentStep = FishingSteps.FIND_ROD;
        MainCounter.reset();
        FailsafeCounter.reset();
        failSafe = false;
        fishingHook = null;
        fishingMarker = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (failSafe) {
            return;
        }

        switch (currentStep) {
            case FIND_ROD: {
                for (int slotIndex = 0; slotIndex < CodecClient.mc.thePlayer.inventory.getSizeInventory(); slotIndex++) {
                    ItemStack stack = CodecClient.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                        CodecClient.mc.thePlayer.inventory.currentItem = slotIndex;
                        currentStep = FishingSteps.LOOK_TO_NEW_BLOCK;
                        return;
                    }
                }

                ChatUtils.sendMessage("Disabled macro -> couldn't find rod.");
                this.unregister();

                return;
            }

            case LOOK_TO_NEW_BLOCK: {
                CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw -4 + Math.random() * 10), 7);
                CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch -4 + Math.random() * 10), 7);

                currentStep = FishingSteps.CAST_HOOK;
                return;
            }

            case CAST_HOOK: {
                CodecClient.mc.playerController.sendUseItem(CodecClient.mc.thePlayer, CodecClient.mc.thePlayer.getEntityWorld(), CodecClient.mc.thePlayer.inventory.getCurrentItem());

                currentStep = FishingSteps.WAIT_FOR_CATCH;
                return;
            }

            case WAIT_FOR_CATCH:
            {
                if (MainCounter.countUntil(20)) {
                    return;
                }

                for (Entity entity : CodecClient.mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityFishHook && ((EntityFishHook) entity).angler == CodecClient.mc.thePlayer) {
                        fishingHook = entity;
                    }
                }

                if (fishingHook == null) {
                    currentStep = FishingSteps.FIND_ROD;
                    return;
                }

                if (fishingMarker != null && fishingMarker.isEntityAlive() && fishingMarker.getName().contains("!!!")) {
                    currentStep = FishingSteps.CATCH;
                    fishingMarker = null;
                    return;
                }

                return;
            }

            case CATCH: {
                CodecClient.mc.playerController.sendUseItem(CodecClient.mc.thePlayer, CodecClient.mc.thePlayer.getEntityWorld(), CodecClient.mc.thePlayer.inventory.getCurrentItem());

                currentStep = FishingSteps.FIND_ROD;
                return;
            }
        }
    }

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
                (
                    event.packet instanceof S19PacketEntityHeadLook && ((S19Accessor) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()
                ) ||
                (
                    event.packet instanceof S1BPacketEntityAttach && ((S1BPacketEntityAttach) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()
                ) ||
                (
                    event.packet instanceof S18PacketEntityTeleport && ((S18PacketEntityTeleport) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()
                )
        ) {
            ChatUtils.sendMessage("Disabled macro -> failsafe has been triggered");
            CodecClient.rotation.reset();
            failSafe = true;
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (!failSafe) {
            return;
        }

        if (!FailsafeCounter.countUntil(120)) {

            CodecClient.mc.thePlayer.playSound("random.anvil_land", 10.f, 1.f);

            switch (FailsafeCounter.get()) {
                case 20: {
                    CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw - 89 + (Math.random() * 180)), 7);
                    CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch - 14 + (Math.random() * 30)), 7);

                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
                    break;
                }

                case 45: {
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    break;
                }

                case 60: {
                    CodecClient.mc.thePlayer.sendChatMessage(FAILSAFE_TEXT[(int) (Math.random() * FAILSAFE_TEXT.length)]);

                    CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw - 89 + (Math.random() * 180)), 7);
                    CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch - 14 + (Math.random() * 30)), 7);
                    break;
                }

                case 80: {
                    CodecClient.rotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw - 89 + (Math.random() * 180)), 7);
                    CodecClient.rotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch - 14 + (Math.random() * 30)), 7);
                    break;
                }

                case 90: {
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                    break;
                }

                case 105: {
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    break;
                }
            }
        } else {
            failSafe = false;
            this.unregister();
        }
    }
}