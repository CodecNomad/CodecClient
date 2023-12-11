package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.events.PacketEvent;
import com.github.codecnomad.codecclient.mixins.S19PacketAccessor;
import com.github.codecnomad.codecclient.ui.Config;
import com.github.codecnomad.codecclient.utils.Math;
import com.github.codecnomad.codecclient.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;

@SuppressWarnings("DuplicatedCode")
public class FishingMacro extends Module {
    public static final String[] FAILSAFE_TEXT = new String[]{"?", "you good?", "HI IM HERE", "can you not bro", "can you dont", "j g growl wtf", "can i get friend request??", "hello i'm here",};
    public static int startTime = 0;
    public static int totalTime = 0;
    public static int catches = 0;
    public static float xpGain = 0;
    public static FishingSteps currentStep = FishingSteps.FIND_ROD;
    public static Counter MainCounter = new Counter();
    public static Counter AlternativeCounter = new Counter();
    public static Counter FailsafeCounter = new Counter();
    public static boolean failSafe = false;
    Entity fishingHook = null;
    Entity fishingMarker = null;
    Entity fishingMonster = null;
    public static float lastYaw = 0;
    public static float lastPitch = 0;
    public static AxisAlignedBB lastAABB = null;

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        this.state = true;
        lastYaw = Client.mc.thePlayer.rotationYaw;
        lastPitch = Client.mc.thePlayer.rotationPitch;
        startTime = (int) java.lang.Math.floor((double) System.currentTimeMillis() / 1000);
        try {
            File reader = new File("fishingHUD.json");
            StringBuilder json = new StringBuilder();
            Scanner myReader = new Scanner(reader);
            while (myReader.hasNextLine()) {
                json.append(myReader.nextLine());
            }
            System.out.println(json.toString());
            myReader.close();
            JSONObject data = new JSONObject(json.toString());
            totalTime = data.getInt("totalTime");
            catches = data.getInt("catches");
            xpGain = data.getFloat("xpGain");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sound.disableSounds();
    }

    @Override
    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.state = false;

        Client.rotation.updatePitch = false;
        Client.rotation.updateYaw = false;
        lastPitch = 0;
        lastYaw = 0;

        Sound.enableSounds();

        currentStep = FishingSteps.FIND_ROD;
        MainCounter.reset();
        FailsafeCounter.reset();
        failSafe = false;
        fishingHook = null;
        fishingMarker = null;
        fishingMonster = null;

        lastAABB = null;

        try{
            File reader = new File("fishingHUD.json");
            StringBuilder json = new StringBuilder();
            Scanner myReader = new Scanner(reader);
            while (myReader.hasNextLine()) {
                json.append(myReader.nextLine());
            }
            System.out.println(json.toString());
            myReader.close();
            JSONObject data = new JSONObject(json.toString());
            totalTime = data.getInt("totalTime") + (int) java.lang.Math.floor((double) System.currentTimeMillis() / 1000) - startTime;
            FileWriter file = new FileWriter("fishingHUD.json");
            file.write(String.format("{\n" +
                                    "  \"totalTime\": %d,\n" +
                                    "  \"catches\": %d,\n" +
                                    "  \"xpGain\": %f\n" +
                                    "}",totalTime, catches, xpGain));
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (failSafe) {
            return;
        }

        switch (currentStep) {
            case FIND_ROD: {
                for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                    ItemStack stack = Client.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                    if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                        Client.rotation.setYaw(lastYaw, Config.RotationSmoothing);
                        Client.rotation.setPitch(lastPitch, Config.RotationSmoothing);
                        Client.mc.thePlayer.inventory.currentItem = slotIndex;
                        currentStep = FishingSteps.CAST_HOOK;
                        return;
                    }
                }

                Chat.sendMessage("Disabled macro -> couldn't find rod.");
                this.unregister();

                return;
            }

            case CAST_HOOK: {
                if (Client.rotation.updateYaw || Client.rotation.updatePitch) {
                    return;
                }

                KeyBinding.onTick(Client.mc.gameSettings.keyBindUseItem.getKeyCode());

                currentStep = FishingSteps.WAIT_FOR_CATCH;
                return;
            }

            case WAIT_FOR_CATCH: {
                if (MainCounter.countUntil(Config.FishingDelay)) {
                    return;
                }

                if (!AlternativeCounter.countUntil(Config.MovementFrequency)) {
                    if (fishingHook == null) {
                        currentStep = FishingSteps.FIND_ROD;
                        return;
                    }

                    AxisAlignedBB aabb = fishingHook.getEntityBoundingBox();

                    double expandedMinX = aabb.minX - 1;
                    double expandedMinY = aabb.minY - 1;
                    double expandedMinZ = aabb.minZ - 1;
                    double expandedMaxX = aabb.maxX + 1;
                    double expandedMaxY = aabb.maxY + 1;
                    double expandedMaxZ = aabb.maxZ + 1;

                    lastAABB = new AxisAlignedBB(expandedMinX, expandedMinY, expandedMinZ, expandedMaxX, expandedMaxY, expandedMaxZ);

                    double randomX = expandedMinX + java.lang.Math.random() * (expandedMaxX - expandedMinX);
                    double randomY = expandedMinY + java.lang.Math.random() * (expandedMaxY - expandedMinY);
                    double randomZ = expandedMinZ + java.lang.Math.random() * (expandedMaxZ - expandedMinZ);

                    Client.rotation.setYaw(Math.getYaw(new BlockPos(randomX, randomY, randomZ)), 5);
                    Client.rotation.setPitch(Math.getPitch(new BlockPos(randomX, randomY, randomZ)), 5);
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
                KeyBinding.onTick(Client.mc.gameSettings.keyBindUseItem.getKeyCode());

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

                Client.mc.thePlayer.inventory.currentItem = Config.WeaponSlot - 1;

                AxisAlignedBB boundingBox = fishingMonster.getEntityBoundingBox();
                double deltaX = boundingBox.maxX - boundingBox.minX;
                double deltaY = boundingBox.maxY - boundingBox.minY;
                double deltaZ = boundingBox.maxZ - boundingBox.minZ;

                BlockPos randomPositionOnBoundingBox = new BlockPos(boundingBox.minX + deltaX, boundingBox.minY + deltaY, boundingBox.minZ + deltaZ);
                Client.rotation.setYaw(Math.getYaw(randomPositionOnBoundingBox), Config.RotationSmoothing);
                Client.rotation.setPitch(Math.getPitch(randomPositionOnBoundingBox), Config.RotationSmoothing);

                if (!MainCounter.countUntil(20 / Config.AttackCps)) {
                    MainCounter.add(java.lang.Math.random() * 100 > 70 ? 1 : 0);
                    if (Config.RightClick) {
                        KeyBinding.onTick(Client.mc.gameSettings.keyBindUseItem.getKeyCode());
                        return;
                    }
                    KeyBinding.onTick(Client.mc.gameSettings.keyBindAttack.getKeyCode());
                }
            }
        }
    }

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (lastAABB != null) {
            Render.drawOutlinedFilledBoundingBox(lastAABB, Config.VisualColor.toJavaColor(), event.partialTicks);
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
            Client.rotation.reset();
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
                    Client.rotation.setYaw((float) (Client.mc.thePlayer.rotationYaw - 89 + (java.lang.Math.random() * 180)), Config.RotationSmoothing);
                    Client.rotation.setPitch((float) (Client.mc.thePlayer.rotationPitch - 14 + (java.lang.Math.random() * 30)), Config.RotationSmoothing);

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

                    Client.rotation.setYaw((float) (Client.mc.thePlayer.rotationYaw - 89 + (java.lang.Math.random() * 180)), Config.RotationSmoothing);
                    Client.rotation.setPitch((float) (Client.mc.thePlayer.rotationPitch - 14 + (java.lang.Math.random() * 30)), Config.RotationSmoothing);
                    break;
                }

                case 80: {
                    Client.rotation.setYaw((float) (Client.mc.thePlayer.rotationYaw - 89 + (java.lang.Math.random() * 180)), Config.RotationSmoothing);
                    Client.rotation.setPitch((float) (Client.mc.thePlayer.rotationPitch - 14 + (java.lang.Math.random() * 30)), Config.RotationSmoothing);
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
        FIND_ROD,
        CAST_HOOK,
        WAIT_FOR_CATCH,
        CATCH,
        KILL_DELAY,
        KILL_MONSTER
    }
}