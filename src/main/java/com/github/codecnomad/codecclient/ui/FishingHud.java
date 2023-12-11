package com.github.codecnomad.codecclient.ui;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.modules.FishingMacro;
import com.github.codecnomad.codecclient.utils.Math;

import static com.github.codecnomad.codecclient.modules.FishingMacro.*;

public class FishingHud extends BasicHud {

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        if (!Client.modules.get("FishingMacro").state) {
            return;
        }

        NanoVGHelper.INSTANCE.setupAndDraw(true, vg -> {
            int elapsedTimeSeconds = (int) (System.currentTimeMillis() / 1000 - startTime) + totalTime;
            int averageCPH = catches != 0 ? (catches * 3600) / (elapsedTimeSeconds != 0 ? elapsedTimeSeconds : 1) : 0;
            int averageXPH = xpGain != 0 ? (int) ((xpGain * 3600) / (elapsedTimeSeconds != 0 ? elapsedTimeSeconds : 1)) : 0;

            NanoVGHelper.INSTANCE.drawLine(vg,
                    x - 5 * scale,
                    y - 5 * scale,
                    x + this.getWidth(this.getScale(), false) + 5 * scale,
                    y - 5 * scale,
                    1 * scale,
                    Config.VisualColor.getRGB()
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    String.format(
                            "FISHING (%s)", Math.toClock(elapsedTimeSeconds)
                    ),
                    x,
                    y + (3 * scale),
                    Config.VisualColor.getRGB(),
                    (6f * scale),
                    Fonts.BOLD
            );
            NanoVGHelper.INSTANCE.drawText(vg,
                    "Average C/H:", x, y + 9 * scale,
                    new OneColor(128, 128, 128).getRGB(),
                    (4f * scale),
                    Fonts.REGULAR)
            ;

            NanoVGHelper.INSTANCE.drawText(vg,
                    String.valueOf(Math.toFancyNumber(averageCPH)),
                    x + (30 * scale),
                    y + (9 * scale),
                    Config.VisualColor.getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    "Total Catches: ",
                    x,
                    y + (13 * scale),
                    new OneColor(128, 128, 128).getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    String.valueOf(Math.toFancyNumber(catches)),
                    x + (30 * scale),
                    y + (13 * scale),
                    Config.VisualColor.getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    "SKILLS",
                    x,
                    y + (22 * scale),
                    Config.VisualColor.getRGB(),
                    (6f * scale),
                    Fonts.BOLD

            );
            NanoVGHelper.INSTANCE.drawText(vg,
                    "Average XP/H:",
                    x,
                    y + (28 * scale),
                    new OneColor(128, 128, 128).getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    String.valueOf(Math.toFancyNumber(averageXPH)),
                    x + (30 * scale),
                    y + (28 * scale),
                    Config.VisualColor.getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    "Total XP:",
                    x,
                    y + (32 * scale),
                    new OneColor(128, 128, 128).getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawText(vg,
                    String.valueOf(Math.toFancyNumber((int) xpGain)),
                    x + (30 * scale),
                    y + (32 * scale),
                    Config.VisualColor.getRGB(),
                    (4f * scale),
                    Fonts.REGULAR
            );

            NanoVGHelper.INSTANCE.drawLine(vg,
                    x - (5 * scale),
                    y + (37 * scale),
                    x + this.getWidth(this.getScale(), false) + (5 * scale),
                    y + (37 * scale), (1 * scale),
                    Config.VisualColor.getRGB());
        });
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        int elapsedTimeSeconds = (int) (System.currentTimeMillis() / 1000 - startTime);
        return (String.format(
                "FISHING (%s)", Math.toClock(elapsedTimeSeconds)
        )).length() * (3 * scale);
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return 32 * scale;
    }

    @Override
    protected boolean shouldDrawBackground() {
        return Client.modules.get("FishingMacro").state;
    }
}
