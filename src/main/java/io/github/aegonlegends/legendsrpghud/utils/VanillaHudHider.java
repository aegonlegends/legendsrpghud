package io.github.aegonlegends.legendsrpghud.utils;

import io.github.aegonlegends.legendsrpghud.LegendsRPGHUDClient;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LegendsRPGHUDClient.MODID, value = Dist.CLIENT)
public class VanillaHudHider {

    @SubscribeEvent
    public static void onLayerPre(RenderGuiLayerEvent.Pre event) {
        ResourceLocation layer = event.getName();

        if (layer.equals(VanillaGuiLayers.PLAYER_HEALTH)
                || layer.equals(VanillaGuiLayers.VEHICLE_HEALTH)
                || layer.equals(VanillaGuiLayers.FOOD_LEVEL)
                || layer.equals(VanillaGuiLayers.EFFECTS)
                || layer.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
            event.setCanceled(true);
        }
    }
}