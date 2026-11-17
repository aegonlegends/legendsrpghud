package io.github.aegonlegends.legendsrpghud.elements;

import io.github.aegonlegends.legendsrpghud.LegendsRPGHUDClient;
import io.github.aegonlegends.legendsrpghud.utils.EffectHelper;
import io.github.aegonlegends.legendsrpghud.utils.PlayerHandler;
import io.github.aegonlegends.legendsrpghud.utils.TimeTextFormatter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = LegendsRPGHUDClient.MODID, value = Dist.CLIENT)
public class EffectBar {
    private EffectBar() {
        /* This utility class should not be instantiated */
    }

    protected static List<MobEffectInstance> buffs = new ArrayList<>();
    protected static List<MobEffectInstance> debuffs = new ArrayList<>();
    protected static List<MobEffectInstance> neutrals = new ArrayList<>();

    private static final int ICON_SIZE = 8;
    private static final int SPACING = 11;

    private static int STARTING_X;
    private static int STARTING_Y;


    @SubscribeEvent
    public static void drawMenu(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(LegendsRPGHUDClient.MODID, "effects_bar"),
                (graphics, deltaTracker) -> {
                    STARTING_X = graphics.guiWidth() / 2;
                    STARTING_Y = 3;

                    updateEffects();
                    drawEffects(graphics);
                });
    }

    public static void updateEffects() {
        Player player = (Player) PlayerHandler.getPlayerEntity();
        if (player == null) return;

        Iterator<MobEffectInstance> effects = player.getActiveEffects().iterator();
        buffs.clear();
        debuffs.clear();
        neutrals.clear();

        if(!effects.hasNext()) return;
        MobEffectInstance effect;

        do{
            effect = effects.next();
            if(!effect.showIcon()) continue;

            switch (effect.getEffect().value().getCategory()){
                case BENEFICIAL :
                    buffs.add(effect); break;
                case HARMFUL:
                    debuffs.add(effect); break;
                case NEUTRAL:
                    neutrals.add(effect);
            }
        } while(effects.hasNext());

        buffs.sort(EffectHelper.EFFECT_COMPARATOR);
        debuffs.sort(EffectHelper.EFFECT_COMPARATOR);
        neutrals.sort(EffectHelper.EFFECT_COMPARATOR);
    }

    private static void drawEffect(GuiGraphics graphics, MobEffectInstance effect, int x, int y) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
        Font font = Minecraft.getInstance().font;

        RenderSystem.enableBlend();
        graphics.blit(x, y, 0, ICON_SIZE, ICON_SIZE, sprite);
        RenderSystem.disableBlend();

        String text = TimeTextFormatter.ticksToCompactTimeString(effect.isInfiniteDuration() ? -1 : effect.getDuration());
        float scale = 0.5f;
        float width = font.width(text) * scale;

        graphics.pose().pushPose();
        graphics.pose().translate(x + (ICON_SIZE/2.0f) - (width/2.0f), y + 2.0f + ICON_SIZE, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, text, 0, 0, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    private static void drawEffects(GuiGraphics graphics){
        MobEffectInstance effect;
        int buffsCount = buffs.size();
        int debuffsCount = debuffs.size();
        int neutralsCount = neutrals.size();

        int neutralX = STARTING_X - ((neutralsCount * ICON_SIZE) / 2);
        int localX = neutralX - (buffsCount * SPACING);
        int localY = STARTING_Y;
        int i;

        if(neutralsCount + debuffsCount > 0) // Space the different Effect
            localX -= SPACING / 2;

        for (i = 0; i < buffsCount; i++) {
            effect = buffs.get(i);
            int spacing = (SPACING * (i));
            drawEffect(graphics, effect, localX + spacing, localY);
        }

        for (i = 0; i < neutralsCount; i++) {
            effect = neutrals.get(i);
            int spacing = (SPACING * (i));
            localX = neutralX + spacing;

            drawEffect(graphics, effect, localX, localY);
        }

        localX = neutralX + (SPACING * (i)); // Set localX to the next effect slot after neutral Effects

        if(neutralsCount + buffsCount > 0) // Space the different Effect clusters
            localX += SPACING / 2;

        for (i = 0; i < debuffsCount; i++) {
            effect = debuffs.get(i);
            int spacing = (SPACING * (i));
            drawEffect(graphics, effect, localX + spacing, localY);
        }
    }
}
