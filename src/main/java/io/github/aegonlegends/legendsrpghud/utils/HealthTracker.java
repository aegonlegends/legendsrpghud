package io.github.aegonlegends.legendsrpghud.utils;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class HealthTracker {
    private int lastTickHealth;
    private int lastDisplayedHealth;
    private long lastChangeMillis;
    private long blinkEndTick;
    private boolean initialized = false;

    private int currentHealth;
    private int tick;

    public void update() {
        Float hp = PlayerHandler.getPlayerHP();
        LivingEntity entity = PlayerHandler.getPlayerEntity();
        if(hp == null || entity == null){
            return;
        }
        currentHealth = Mth.ceil(hp);
        tick = entity.tickCount;
        long now = Util.getMillis();

        if(initialized){
            if (currentHealth < lastTickHealth) {
                lastChangeMillis = now;
                blinkEndTick = tick + 20;
            } else if (currentHealth > lastTickHealth) {
                lastChangeMillis = now;
                blinkEndTick = tick + 10;
            }
        }
        else {
            lastDisplayedHealth = currentHealth;
            lastTickHealth = currentHealth;
            initialized = true;
        }

        if (now - lastChangeMillis > 1000L) {
            lastDisplayedHealth = currentHealth;
            lastChangeMillis = now;
        }
        lastTickHealth = currentHealth;
    }

    public void reset() {
        initialized = false;
        lastTickHealth = 0;
        lastDisplayedHealth = 0;
        lastChangeMillis = 0;
        blinkEndTick = 0;
        currentHealth = 0;
        tick = 0;
    }

    public boolean isBlinking() {
        return blinkEndTick > tick && (blinkEndTick - tick) / 3 % 2 == 1;
    }

    public int health() { return currentHealth; }
    public int previousHealth() { return lastDisplayedHealth; }
}
