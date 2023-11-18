package com.github.codecnomad.codecclient.modules;

import com.github.codecnomad.codecclient.CodecClient;
import com.github.codecnomad.codecclient.Module;
import com.github.codecnomad.codecclient.utils.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class FishingMacro extends Module {
    private static final int SEARCHING_ROD_STATE = -1;
    private static final int CASTING_STATE = 0;
    private static final int HOOK_CHECK_STATE = 1;
    private int currentState = SEARCHING_ROD_STATE;
    private long castTime = 0;
    private int counter = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        EntityPlayer player = CodecClient.mc.thePlayer;
        if (player == null || counter < 20) {
            counter++;
            return;
        } counter = 0;

        switch (currentState) {
            case SEARCHING_ROD_STATE:
                findAndSelectFishingRod(player);
                break;

            case CASTING_STATE:
                castRod(player);
                break;

            case HOOK_CHECK_STATE:
                checkHookState(player);
                break;
        }
    }

    private void findAndSelectFishingRod(EntityPlayer player) {
        ChatUtils.sendMessage("Searching for a fishing rod...");
        for (int slotIndex = 0; slotIndex < player.inventory.getSizeInventory(); slotIndex++) {
            ItemStack stack = player.inventory.getStackInSlot(slotIndex);
            if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                ChatUtils.sendMessage("Found a fishing rod!");
                player.inventory.currentItem = slotIndex;
                currentState = CASTING_STATE;
                return;
            }
        }
        ChatUtils.sendMessage("Failed to find a fishing rod...");
        this.unregister();
    }

    private void castRod(EntityPlayer player) {
        ChatUtils.sendMessage("Casting the fishing rod...");
        CodecClient.mc.playerController.sendUseItem(player, player.getEntityWorld(), player.inventory.getCurrentItem());
        castTime = System.currentTimeMillis();
        currentState = HOOK_CHECK_STATE;
    }

    private void checkHookState(EntityPlayer player) {
        EntityFishHook fishingHook = findFishingHook(player);
        if (fishingHook == null) {
            ChatUtils.sendMessage("Couldn't find the fishing hook...");
            currentState = SEARCHING_ROD_STATE;
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (isFishCaught()) {
            ChatUtils.sendMessage("Fish caught! Took: " + ((currentTime - castTime) / 1000) + "s");
            CodecClient.mc.playerController.sendUseItem(player, player.getEntityWorld(), player.inventory.getCurrentItem());
            currentState = SEARCHING_ROD_STATE;
            fishingMarker = null;
        }
    }

    private EntityFishHook findFishingHook(EntityPlayer player) {
        for (Entity entity : CodecClient.mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityFishHook && ((EntityFishHook) entity).angler == player) {
                return (EntityFishHook) entity;
            }
        }
        return null;
    }

    // Best detection
    private Entity fishingMarker = null;
    private boolean isFishCaught() {
        if (fishingMarker == null || !fishingMarker.isEntityAlive()) {return false;}
        return fishingMarker.getName().contains("!!!");
    }

    @SubscribeEvent
    public void entitySpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityArmorStand) {
            fishingMarker = event.entity;
        }
    }
}
