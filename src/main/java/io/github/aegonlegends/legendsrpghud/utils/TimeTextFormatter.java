package io.github.aegonlegends.legendsrpghud.utils;

import net.minecraft.util.Mth;

public final class TimeTextFormatter {
    private TimeTextFormatter() {}   // static helpers only, no instances

    public static String ticksToCompactTimeString(int ticks) {
        if(ticks == -1) return "∞";
        if (ticks >= 20 * 60 * 60 * 24) return "24h"; // Anything over 24h is treated as infinite for displaying purposes

        int seconds = Mth.ceil(ticks / 20f);
        if (seconds < 60) return seconds + "s";

        int minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";

        return (minutes / 60) + "h";
    }
}
