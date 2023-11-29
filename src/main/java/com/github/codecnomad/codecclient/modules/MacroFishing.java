package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.classes.HelperClassCounter;
import com.github.codecnomad.codecclient.classes.HelperClassModule;
import com.github.codecnomad.codecclient.classes.HelperClassPacketEvent;
import com.github.codecnomad.codecclient.guis.GuiConfig;
import com.github.codecnomad.codecclient.mixins.AccessorS19;
import com.github.codecnomad.codecclient.utils.UtilChat;
import com.github.codecnomad.codecclient.utils.UtilMath;
import com.github.codecnomad.codecclient.utils.UtilRender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MacroFishing extends HelperClassModule {
    public static final String[] FAILSAFE_TEXT = new String[]{"?", "you good?", "HI IM HERE", "can you not bro", "can you dont", "j g growl wtf", "can i get friend request??", "hello i'm here",};
    public static int startTime = 0;
    public static int catches = 0;
    public static float xpGain = 0;
    public FishingSteps currentStep = FishingSteps.FIND_ROD;
    public HelperClassCounter MainCounter = new HelperClassCounter();
    public HelperClassCounter FailsafeCounter = new HelperClassCounter();
    public boolean failSafe = false;
    Entity fishingHook = null;
    Entity fishingMarker = null;
    Entity fishingMonster = null;
    List<BlockPos> waterBlocks = new ArrayList<>();
    BlockPos currentWaterBlock = null;

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        this.state = true;

        startTime = (int) Math.floor((double) System.currentTimeMillis() / 1000);
    }

    @Override
    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.state = false;

        CodecClient.helperClassRotation.updatePitch = false;
        CodecClient.helperClassRotation.updateYaw = false;

        startTime = 0;
        catches = 0;
        xpGain = 0;

        currentStep = FishingSteps.FIND_WATER;
        MainCounter.reset();
        FailsafeCounter.reset();
        failSafe = false;
        fishingHook = null;
        fishingMarker = null;
        fishingMonster = null;
        waterBlocks.clear();
        currentWaterBlock = null;

    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (failSafe) {
            return;
        }

        switch (currentStep) {
            case FIND_WATER: {
                for (int x = -8; x <= 8; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -8; z <= 8; z++) {
                            BlockPos pos = new BlockPos(
                                    CodecClient.mc.thePlayer.posX + x,
                                    CodecClient.mc.thePlayer.posY + y,
                                    CodecClient.mc.thePlayer.posZ + z
                            );

                            Block block = CodecClient.mc.theWorld.getBlockState(pos).getBlock();

                            if ((block instanceof BlockStaticLiquid || block instanceof BlockDynamicLiquid)
                                    && CodecClient.mc.theWorld.rayTraceBlocks(
                                    new Vec3(CodecClient.mc.thePlayer.posX, CodecClient.mc.thePlayer.posY + CodecClient.mc.thePlayer.getEyeHeight() / 2, CodecClient.mc.thePlayer.posZ),
                                    new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                                    false, true, false
                            ) == null) {
                                waterBlocks.add(pos);
                            }
                        }
                    }
                }

                currentStep = FishingSteps.FIND_ROD;
                return;
            }

            case FIND_ROD: {
                for (int slotIndex = 0; slotIndex < CodecClient.mc.thePlayer.inventory.getSizeInventory(); slotIndex++) {
                    ItemStack stack = CodecClient.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                        CodecClient.mc.thePlayer.inventory.currentItem = slotIndex;
                        currentStep = FishingSteps.LOOK_TO_NEW_BLOCK;
                        return;
                    }
                }

                UtilChat.sendMessage("Disabled macro -> couldn't find rod.");
                this.unregister();

                return;
            }

            case LOOK_TO_NEW_BLOCK: {
                if (waterBlocks.isEmpty()) {
                    UtilChat.sendMessage("Disabled macro -> couldn't find any water.");
                    this.unregister();
                    return;
                }

                BlockPos randomWater = waterBlocks.get((int) (Math.random() * waterBlocks.size()));
                currentWaterBlock = randomWater;
                CodecClient.helperClassRotation.setYaw((float) (UtilMath.getYaw(randomWater) - 2 + Math.random() * 3), GuiConfig.RotationSmoothing);
                CodecClient.helperClassRotation.setPitch((float) (UtilMath.getPitch(randomWater) - 2 + Math.random() * 3), GuiConfig.RotationSmoothing);

                currentStep = FishingSteps.CAST_HOOK;
                return;
            }

            case CAST_HOOK: {
                if (CodecClient.helperClassRotation.updateYaw || CodecClient.helperClassRotation.updatePitch) {
                    return;
                }

                CodecClient.mc.playerController.sendUseItem(CodecClient.mc.thePlayer, CodecClient.mc.thePlayer.getEntityWorld(), CodecClient.mc.thePlayer.inventory.getCurrentItem());

                currentStep = FishingSteps.WAIT_FOR_CATCH;
                return;
            }

            case WAIT_FOR_CATCH: {
                if (MainCounter.countUntil(GuiConfig.FishingDelay)) {
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

                if (GuiConfig.AutoKill) {
                    currentStep = FishingSteps.KILL_DELAY;
                } else {
                    currentStep = FishingSteps.FIND_ROD;
                }
                catches++;
            }

            case KILL_DELAY: {
                if (MainCounter.countUntil(GuiConfig.KillDelay)) {
                    return;
                }

                currentStep = FishingSteps.KILL_MONSTER;
            }

            case KILL_MONSTER: {
                if (fishingMonster == null || !fishingMonster.isEntityAlive() || !CodecClient.mc.thePlayer.canEntityBeSeen(fishingMonster)) {
                    currentStep = FishingSteps.FIND_ROD;
                    fishingMonster = null;
                    return;
                }

                for (int slotIndex = 0; slotIndex < CodecClient.mc.thePlayer.inventory.getSizeInventory(); slotIndex++) {
                    ItemStack stack = CodecClient.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null &&
                            (
                                    stack.getItem() instanceof ItemSpade ||
                                            stack.getItem() instanceof ItemSword ||
                                            stack.getItem() instanceof ItemAxe
                            )
                    ) {
                        CodecClient.mc.thePlayer.inventory.currentItem = slotIndex;
                        break;
                    }
                }

                CodecClient.helperClassRotation.setYaw((float) (UtilMath.getYaw(fishingMonster.getPosition()) - 1 + Math.random() * 2), GuiConfig.RotationSmoothing);
                CodecClient.helperClassRotation.setPitch((float) (UtilMath.getPitch(fishingMonster.getPosition().add(0, fishingMonster.getEyeHeight(), 0)) - 1 + Math.random() * 2), GuiConfig.RotationSmoothing);

                if (!MainCounter.countUntil(20 / GuiConfig.AttackCps)) {
                    MainCounter.add(Math.random() * 100 > 70 ? 1 : 0);
                    KeyBinding.onTick(CodecClient.mc.gameSettings.keyBindAttack.getKeyCode());
                }
            }
        }
    }

    @SubscribeEvent
    public void renderLast(RenderWorldLastEvent event) {
        if (currentWaterBlock != null) {
            GlStateManager.disableDepth();
            UtilRender.drawOutlinedFilledBoundingBox(
                    currentWaterBlock,
                    GuiConfig.VisualColor.toJavaColor(),
                    event.partialTicks);
            GlStateManager.enableDepth();
        }
    }

    @SubscribeEvent
    public void entitySpawn(EntityJoinWorldEvent event) {
        if (fishingHook == null || event.entity instanceof EntitySquid || event.entity.getName().equals("item.tile.stone.stone")) {
            return;
        }

        if (event.entity instanceof EntityArmorStand && event.entity.getDistanceToEntity(fishingHook) <= 0.1) {
            fishingMarker = event.entity;
            return;
        }

        if (fishingMonster == null &&
                event.entity.getDistanceToEntity(fishingHook) <= 1.5
        ) {
            fishingMonster = event.entity;
        }
    }

    @SubscribeEvent
    public void chatReceive(ClientChatReceivedEvent event) {
        if (event.type != 2) {
            return;
        }

        Matcher matcher = UtilChat.FishingSkillPattern.matcher(event.message.getFormattedText());
        if (matcher.find()) {
            xpGain += Float.parseFloat(matcher.group(1));
        }
    }

    @SubscribeEvent
    public void packetReceive(HelperClassPacketEvent.ReceiveEventHelperClass event) {
        if (
                event.packet instanceof S08PacketPlayerPosLook ||
                        event.packet instanceof S09PacketHeldItemChange ||
                        (
                                event.packet instanceof S19PacketEntityHeadLook && ((AccessorS19) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()
                        ) ||
                        (
                                event.packet instanceof S1BPacketEntityAttach && ((S1BPacketEntityAttach) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()
                        ) ||
                        (
                                event.packet instanceof S18PacketEntityTeleport && ((S18PacketEntityTeleport) event.packet).getEntityId() == CodecClient.mc.thePlayer.getEntityId()
                        )
        ) {
            UtilChat.sendMessage("Disabled macro -> failsafe has been triggered");
            CodecClient.helperClassRotation.reset();
            failSafe = true;
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (!failSafe) {
            return;
        }

        if (FailsafeCounter.countUntil(120)) {

            CodecClient.mc.thePlayer.playSound("random.anvil_land", 10.f, 1.f);

            switch (FailsafeCounter.get()) {
                case 20: {
                    CodecClient.helperClassRotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw - 89 + (Math.random() * 180)), GuiConfig.RotationSmoothing);
                    CodecClient.helperClassRotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch - 14 + (Math.random() * 30)), GuiConfig.RotationSmoothing);

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

                    CodecClient.helperClassRotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw - 89 + (Math.random() * 180)), GuiConfig.RotationSmoothing);
                    CodecClient.helperClassRotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch - 14 + (Math.random() * 30)), GuiConfig.RotationSmoothing);
                    break;
                }

                case 80: {
                    CodecClient.helperClassRotation.setYaw((float) (CodecClient.mc.thePlayer.rotationYaw - 89 + (Math.random() * 180)), GuiConfig.RotationSmoothing);
                    CodecClient.helperClassRotation.setPitch((float) (CodecClient.mc.thePlayer.rotationPitch - 14 + (Math.random() * 30)), GuiConfig.RotationSmoothing);
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

    public enum FishingSteps {
        FIND_WATER,
        FIND_ROD,
        LOOK_TO_NEW_BLOCK,
        CAST_HOOK,
        WAIT_FOR_CATCH,
        CATCH,
        KILL_DELAY,
        KILL_MONSTER
    }
}