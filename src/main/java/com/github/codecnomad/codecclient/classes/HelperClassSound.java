package com.github.codecnomad.codecclient.classes;

import com.github.codecnomad.codecclient.CodecClient;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.settings.GameSettings;

public class HelperClassSound {
    private static float previousSoundLevel = 0;

    public static void disableSounds() {
        GameSettings settings = CodecClient.mc.gameSettings;
        previousSoundLevel = settings.getSoundLevel(SoundCategory.MASTER);
        settings.setSoundLevel(SoundCategory.MASTER, 0);
    }

    public static void enableSounds() {
        CodecClient.mc.gameSettings.setSoundLevel(SoundCategory.MASTER, previousSoundLevel);
    }
}
