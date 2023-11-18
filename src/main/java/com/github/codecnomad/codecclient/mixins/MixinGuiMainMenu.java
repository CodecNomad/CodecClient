package com.github.codecnomad.codecclient.mixins;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.item.ItemFishingRod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFishingRod.class)
public class MixinGuiMainMenu {

    @Inject(method = "", at = @At("HEAD"))
    public void onInitGui(CallbackInfo ci) {
        System.out.println("Hello from Main Menu!");
    }
}
