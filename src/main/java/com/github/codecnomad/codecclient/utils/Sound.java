package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.settings.GameSettings;

public class Sound {
    private static float previousSoundLevel = 0;

    public static void disableSounds() {
        GameSettings settings = Client.mc.gameSettings;
        previousSoundLevel = settings.getSoundLevel(SoundCategory.MASTER);
        settings.setSoundLevel(SoundCategory.MASTER, 0);
    }

    public static void enableSounds() {
        Client.mc.gameSettings.setSoundLevel(SoundCategory.MASTER, previousSoundLevel);
    }
}
