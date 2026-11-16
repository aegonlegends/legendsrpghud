package io.github.aegonlegends.legendsrpghud.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class PetFinder {
    private static final double WOLF_PRIORITY_RANGE = 16*16;

    public static LivingEntity find(Player player) {
        if (player.getVehicle() instanceof LivingEntity mount && mount.isAlive()) {
            if (mount instanceof AbstractHorse)
                return mount;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        List<LivingEntity> owned = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof OwnableEntity pet
                    && entity instanceof LivingEntity living
                    && living.isAlive()
                    && player.getUUID().equals(pet.getOwnerUUID())) {
                owned.add(living);
            }
        }
        owned.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));

        for (LivingEntity pet : owned) {
            if (pet instanceof Wolf && pet.distanceToSqr(player) <= WOLF_PRIORITY_RANGE) return pet;
        }
        for (LivingEntity pet : owned) {
            if (pet instanceof AbstractHorse) return pet;
        }

        return owned.isEmpty() ? null : owned.getFirst();
    }
}
