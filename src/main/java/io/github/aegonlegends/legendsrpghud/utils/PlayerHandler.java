package io.github.aegonlegends.legendsrpghud.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;

public class PlayerHandler {
    private PlayerHandler() {
        /* This utility class should not be instantiated */
    }

    public static PlayerSkin getPlayerSkin() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.getSkin();
    }

    public static PlayerSkin getPlayerSkin(String playerName) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return null;

        PlayerInfo player = connection.getPlayerInfo(playerName);
        if (player == null) return null;
        return player.getSkin();
    }

    public static String getPlayerName() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.getDisplayName().getString();
    }

    public static Integer getPlayerLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.experienceLevel;
    }

    public static Float getPlayerMaxHP() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.getMaxHealth();
    }
    public static Float getPlayerHP() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player.getHealth();
    }

    public static LivingEntity getPlayerEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        return mc.player;
    }
}
