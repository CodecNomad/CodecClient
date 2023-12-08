package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.ui.Config;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class Walker {
    List<BlockPos> wayPoints;
    Runnable callback;
    int currentPoint = 0;

    public void start() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void stop() {
        currentPoint = 0;
        MinecraftForge.EVENT_BUS.unregister(this);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindJump.getKeyCode(), false);
        callback.run();
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (currentPoint >= wayPoints.size()) {
            stop();
            return;
        }

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindForward.getKeyCode(), true);

        if (wayPoints.get(currentPoint).getY() + 1 > Client.mc.thePlayer.posY + 2) {
            for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                ItemStack stack = Client.mc.thePlayer.inventory.getStackInSlot(slotIndex);
                if (stack != null && stack.getItem() instanceof ItemSpade && stack.getDisplayName().contains("Aspect of the Void")) {
                    Client.mc.thePlayer.inventory.currentItem = slotIndex;
                    break;
                }
            }

            KeyBinding.onTick(Client.mc.gameSettings.keyBindUseItem.getKeyCode());
        }

        KeyBinding.setKeyBindState(Client.mc.gameSettings.keyBindJump.getKeyCode(), java.lang.Math.abs(Client.mc.thePlayer.motionX) + java.lang.Math.abs(Client.mc.thePlayer.motionZ) < 0.1);

        Client.rotation.setYaw(Math.getYaw(wayPoints.get(currentPoint).add(0, 1, 0)), Config.RotationSmoothing);
        Client.rotation.setPitch(Math.getPitch(wayPoints.get(currentPoint).add(0, 1, 0)), Config.RotationSmoothing);

        if (Client.mc.thePlayer.getDistanceSq(wayPoints.get(currentPoint).add(0, 1, 0)) < 2) {
            currentPoint++;
        }
    }

    public Walker(List<BlockPos> wayPoints, Runnable callback) {
        this.wayPoints = wayPoints;
        this.callback = callback;
    }
}
