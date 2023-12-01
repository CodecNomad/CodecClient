package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.utils.*;
import com.github.codecnomad.codecclient.events.PacketEvent;
import com.github.codecnomad.codecclient.ui.Config;
import com.github.codecnomad.codecclient.mixins.S19PacketAccessor;
import com.github.codecnomad.codecclient.utils.Math;
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
import net.minecraft.util.AxisAlignedBB;
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

@SuppressWarnings("DuplicatedCode")
public class FishingMacro extends Module {
    public static final String[] FAILSAFE_TEXT = new String[]{"?", "you good?", "HI IM HERE", "can you not bro", "can you dont", "j g growl wtf", "can i get friend request??", "hello i'm here",};
    public static int startTime = 0;
    public static int catches = 0;
    public static float xpGain = 0;
    public FishingSteps currentStep = FishingSteps.FIND_ROD;
    public Counter MainCounter = new Counter();
    public Counter FailsafeCounter = new Counter();
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

        Sound.disableSounds();

        startTime = (int) java.lang.Math.floor((double) System.currentTimeMillis() / 1000);
    }

    @Override
    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.state = false;

        Client.helperClassRotation.updatePitch = false;
        Client.helperClassRotation.updateYaw = false;

        Sound.enableSounds();

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
                                    Client.mc.thePlayer.posX + x,
                                    Client.mc.thePlayer.posY + y,
                                    Client.mc.thePlayer.posZ + z
                            );

                            Block block = Client.mc.theWorld.getBlockState(pos).getBlock();

                            if ((block instanceof BlockStaticLiquid || block instanceof BlockDynamicLiquid)
                                    && Client.mc.theWorld.rayTraceBlocks(
                                    new Vec3(Client.mc.thePlayer.posX, Client.mc.thePlayer.posY + Client.mc.thePlayer.getEyeHeight() / 2, Client.mc.thePlayer.posZ),
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
                for (int slotIndex = 0; slotIndex < Client.mc.thePlayer.inventory.getSizeInventory(); slotIndex++) {
                    ItemStack stack = Client.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                        Client.mc.thePlayer.inventory.currentItem = slotIndex;
                        currentStep = FishingSteps.LOOK_TO_NEW_BLOCK;
                        return;
                    }
                }

                Chat.sendMessage("Disabled macro -> couldn't find rod.");
                this.unregister();

                return;
            }

            case LOOK_TO_NEW_BLOCK: {
                if (waterBlocks.isEmpty()) {
                    Chat.sendMessage("Disabled macro -> couldn't find any water.");
                    this.unregister();
                    return;
                }

                BlockPos randomWater = waterBlocks.get((int) (java.lang.Math.random() * waterBlocks.size()));
                currentWaterBlock = randomWater;
                Client.helperClassRotation.setYaw((float) (Math.getYaw(randomWater) - 2 + java.lang.Math.random() * 3), Config.RotationSmoothing);
                Client.helperClassRotation.setPitch((float) (Math.getPitch(randomWater) - 2 + java.lang.Math.random() * 3), Config.RotationSmoothing);

                currentStep = FishingSteps.CAST_HOOK;
                return;
            }

            case CAST_HOOK: {
                if (Client.helperClassRotation.updateYaw || Client.helperClassRotation.updatePitch) {
                    return;
                }

                Client.mc.playerController.sendUseItem(Client.mc.thePlayer, Client.mc.thePlayer.getEntityWorld(), Client.mc.thePlayer.inventory.getCurrentItem());

                currentStep = FishingSteps.WAIT_FOR_CATCH;
                return;
            }

            case WAIT_FOR_CATCH: {
                if (MainCounter.countUntil(Config.FishingDelay)) {
                    return;
                }

                for (Entity entity : Client.mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityFishHook && ((EntityFishHook) entity).angler == Client.mc.thePlayer) {
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
                Client.mc.playerController.sendUseItem(Client.mc.thePlayer, Client.mc.thePlayer.getEntityWorld(), Client.mc.thePlayer.inventory.getCurrentItem());

                if (Config.AutoKill) {
                    currentStep = FishingSteps.KILL_DELAY;
                } else {
                    currentStep = FishingSteps.FIND_ROD;
                }
                catches++;
            }

            case KILL_DELAY: {
                if (MainCounter.countUntil(Config.KillDelay)) {
                    return;
                }

                currentStep = FishingSteps.KILL_MONSTER;
            }

            case KILL_MONSTER: {
                if (fishingMonster == null || !fishingMonster.isEntityAlive() || !Client.mc.thePlayer.canEntityBeSeen(fishingMonster)) {
                    currentStep = FishingSteps.FIND_ROD;
                    fishingMonster = null;
                    return;
                }

                for (int slotIndex = 0; slotIndex < Client.mc.thePlayer.inventory.getSizeInventory(); slotIndex++) {
                    ItemStack stack = Client.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null &&
                            (
                                    stack.getItem() instanceof ItemSpade ||
                                            stack.getItem() instanceof ItemSword ||
                                            stack.getItem() instanceof ItemAxe
                            )
                    ) {
                        Client.mc.thePlayer.inventory.currentItem = slotIndex;
                        break;
                    }
                }

                AxisAlignedBB boundingBox = fishingMonster.getEntityBoundingBox();
                double deltaX = boundingBox.maxX - boundingBox.minX;
                double deltaY = boundingBox.maxY - boundingBox.minY;
                double deltaZ = boundingBox.maxZ - boundingBox.minZ;

                BlockPos randomPositionOnBoundingBox = new BlockPos(boundingBox.minX + deltaX, boundingBox.minY + deltaY, boundingBox.minZ + deltaZ);
                Client.helperClassRotation.setYaw(Math.getYaw(randomPositionOnBoundingBox), Config.RotationSmoothing);
                Client.helperClassRotation.setPitch(Math.getPitch(randomPositionOnBoundingBox), Config.RotationSmoothing);

                if (!MainCounter.countUntil(20 / Config.AttackCps)) {
                    MainCounter.add(java.lang.Math.random() * 100 > 70 ? 1 : 0);
                    KeyBinding.onTick(Client.mc.gameSettings.keyBindAttack.getKeyCode());
                }
            }
        }
    }

    @SubscribeEvent
    public void renderLast(RenderWorldLastEvent event) {
        if (currentWaterBlock != null) {
            GlStateManager.disableDepth();
            Render.drawOutlinedFilledBoundingBox(
                    currentWaterBlock,
                    Config.VisualColor.toJavaColor(),
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

        Matcher matcher = Regex.FishingSkillPattern.matcher(event.message.getFormattedText());
        if (matcher.find()) {
            xpGain += Float.parseFloat(matcher.group(1));
        }
    }

    @SubscribeEvent
    public void packetReceive(PacketEvent.ReceiveEvent event) {
        if (
                event.packet instanceof S08PacketPlayerPosLook ||
                        event.packet instanceof S09PacketHeldItemChange ||
                        (
                                event.packet instanceof S19PacketEntityHeadLook && ((S19PacketAccessor) event.packet).getEntityId() == Client.mc.thePlayer.getEntityId()
                        ) ||
                        (
                                event.packet instanceof S1BPacketEntityAttach && ((S1BPacketEntityAttach) event.packet).getEntityId() == Client.mc.thePlayer.getEntityId()
                        ) ||
                        (
                                event.packet instanceof S18PacketEntityTeleport && ((S18PacketEntityTeleport) event.packet).getEntityId() == Client.mc.thePlayer.getEntityId()
                        )
        ) {
            Chat.sendMessage("Disabled macro -> failsafe has been triggered");
            Client.helperClassRotation.reset();
            failSafe = true;
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (!failSafe) {
            return;
        }

        if (FailsafeCounter.countUntil(120)) {

            Sound.enableSounds();
            Client.mc.thePlayer.playSound("random.anvil_land", 10.f, 1.f);

            if (Config.OnlySound) {
                return;
            }

            switch (FailsafeCounter.get()) {
                case 20: {
                    Client.helperClassRotation.setYaw((float) (Client.mc.thePlayer.rotationYaw - 89 + (java.lang.Math.random() * 180)), Config.RotationSmoothing);
                    Client.helperClassRotation.setPitch((float) (Client.mc.thePlayer.rotationPitch - 14 + (java.lang.Math.random() * 30)), Config.RotationSmoothing);

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
                    Client.mc.thePlayer.sendChatMessage(FAILSAFE_TEXT[(int) (java.lang.Math.random() * FAILSAFE_TEXT.length)]);

                    Client.helperClassRotation.setYaw((float) (Client.mc.thePlayer.rotationYaw - 89 + (java.lang.Math.random() * 180)), Config.RotationSmoothing);
                    Client.helperClassRotation.setPitch((float) (Client.mc.thePlayer.rotationPitch - 14 + (java.lang.Math.random() * 30)), Config.RotationSmoothing);
                    break;
                }

                case 80: {
                    Client.helperClassRotation.setYaw((float) (Client.mc.thePlayer.rotationYaw - 89 + (java.lang.Math.random() * 180)), Config.RotationSmoothing);
                    Client.helperClassRotation.setPitch((float) (Client.mc.thePlayer.rotationPitch - 14 + (java.lang.Math.random() * 30)), Config.RotationSmoothing);
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