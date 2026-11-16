package io.github.aegonlegends.legendsrpghud.elements;

import io.github.aegonlegends.legendsrpghud.LegendsRPGHUDClient;
import io.github.aegonlegends.legendsrpghud.utils.HealthTracker;
import io.github.aegonlegends.legendsrpghud.utils.PetFinder;
import io.github.aegonlegends.legendsrpghud.utils.PlayerHandler;
import io.github.aegonlegends.legendsrpghud.utils.TimeTextFormatter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import net.minecraft.client.gui.GuiGraphics;

import java.util.*;

@EventBusSubscriber(modid = LegendsRPGHUDClient.MODID, value = Dist.CLIENT)
public class PartyMenu {

    private static final int STARTING_X = 4, STARTING_Y = 24;

    private static final RandomSource SHAKE_ANIM_RANDOM = RandomSource.create();
    private static final float REGEN_CYCLE_PADDING = 5f;
    private static final int ICON_SIZE = 8;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LegendsRPGHUDClient.MODID, "textures/hud/backgrounds/partylist.png");
    private static final Map<String, ResourceLocation> HEARTS_ICONS;
    static {
        Map<String, ResourceLocation> map = new HashMap<>();

        map.put("base", ResourceLocation.withDefaultNamespace("hud/heart/container"));
        map.put("base_blink", ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"));

        for (String variant : new String[]{"", "poisoned_", "withered_"}) {
            for (String shape : new String[]{"full", "half"}) {
                map.put(variant + shape,
                        ResourceLocation.withDefaultNamespace("hud/heart/" + variant + shape));
                map.put(variant + shape + "_blink",
                        ResourceLocation.withDefaultNamespace("hud/heart/" + variant + shape + "_blinking"));
            }
        }
        for (String shape : new String[]{"full", "half"}) {
            map.put("absorbing_" + shape,
                    ResourceLocation.fromNamespaceAndPath(LegendsRPGHUDClient.MODID, "textures/hud/hearts/absorption_" + shape + ".png"));
            map.put("vehicle_" + shape,
                    ResourceLocation.withDefaultNamespace("hud/heart/" + "vehicle_" + shape));
        }
        HEARTS_ICONS = Map.copyOf(map);
    }
    private static final Map<String, ResourceLocation> FOOD_ICONS;
    static {
        Map<String, ResourceLocation> map = new HashMap<>();
        for (String variant : new String[]{"", "_hunger"}) {
            for (String shape : new String[]{"empty", "half", "full"}) {
                map.put(shape + variant,
                        ResourceLocation.withDefaultNamespace("hud/food_" + shape + variant));
            }
        }
        FOOD_ICONS = Map.copyOf(map);
    }
    private static final HealthTracker HealthData = new HealthTracker();

    private static final int MAX_EFFECTS = 3;
    private static final int DEBUFF_PRIORITY_SLOTS = 2;
    private static final int BUFF_PRIORITY_SLOTS = 1;


    @SubscribeEvent
    public static void drawMenu(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(LegendsRPGHUDClient.MODID, "party_menu"),
                (graphics, deltaTracker) -> {
                    drawPlayer(graphics);
                    boolean petDrawn = drawPet(graphics);
                });
    }

    private static void drawPlayer(GuiGraphics graphics) {
        drawBackground(graphics, 0);
        drawFace(graphics);
        drawLevelAndName(graphics);
        drawHPBar(graphics);
        drawFood(graphics);
        drawEffects(graphics);
    }

    private static boolean drawPet(GuiGraphics graphics) {
        LivingEntity pet = PetFinder.find((LocalPlayer) PlayerHandler.getPlayerEntity());
        if(pet == null) return false;
        drawBackground(graphics);
        drawPetFace(graphics, pet);
        drawPetName(graphics, pet);
        drawPetHPBar(graphics, pet);
        return true;
    }

    private static void drawBackground(GuiGraphics graphics, int i, boolean petBar) {
        int texW = 128, texH = 64;
        int cropW = 0, cropH = 36;
        int extraW = 16;

        if(petBar){
            cropH += 4;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(BACKGROUND, STARTING_X, STARTING_Y + (28 * i),
                (texW - cropW) + extraW, texH - cropH,
                cropW, cropH,
                texW - cropW, texH - cropH,
                texW, texH);
        RenderSystem.disableBlend();
    }
    private static void drawBackground(GuiGraphics graphics, int i) {
        drawBackground(graphics, i, false);
    }
    // Hardcoded pet version
    private static void drawBackground(GuiGraphics graphics) {
        drawBackground(graphics, 1, true);
    }

    private static void drawFace(GuiGraphics graphics) {
        PlayerSkin skin = PlayerHandler.getPlayerSkin();
        if(skin == null) return;
        PlayerFaceRenderer.draw(graphics, skin, STARTING_X +3, STARTING_Y +4, 20);
    }
    private static void drawPetFace(GuiGraphics graphics, Entity pet) {
        Item itemForIcon;
        if (pet.getType() == EntityType.WOLF) itemForIcon = Items.BONE;
        else if (pet instanceof AbstractHorse) itemForIcon = Items.SADDLE;
        else
            itemForIcon = SpawnEggItem.byId(pet.getType());
        if (itemForIcon == null) return;

        graphics.pose().pushPose();
        graphics.pose().translate(STARTING_X + 19, STARTING_Y + 30, 0);
        graphics.pose().scale(1, 1, 1);
        graphics.renderItem(new ItemStack(itemForIcon), 0, 0);
        graphics.pose().popPose();
    }

    private static void drawLevelAndName(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        String playerName = PlayerHandler.getPlayerName();
        Integer playerLevel = PlayerHandler.getPlayerLevel();
        if(playerName == null || playerLevel == null) return;
        MutableComponent playerNameText = Component.literal(playerName);
        MutableComponent playerLevelText = Component.literal("Lv " + playerLevel + " ").withStyle(ChatFormatting.ITALIC);

        graphics.pose().pushPose();
        graphics.pose().translate(STARTING_X +28, STARTING_Y +2, 0);
        graphics.pose().scale(0.75f, 0.75f, 1.0f);

        graphics.drawString(font, playerLevelText.append(playerNameText),0,0, 0xFFFFFFFF);

        graphics.pose().popPose();
    }
    private static void drawPetName(GuiGraphics graphics, Entity pet) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        String name = pet.getName().getString();
        MutableComponent playerNameText = Component.literal(name);

        graphics.pose().pushPose();
        graphics.pose().translate(STARTING_X +27+11, STARTING_Y + 2 + 28, 0);
        graphics.pose().scale(0.75f, 0.75f, 1.0f);

        graphics.drawString(font,playerNameText,0,0, 0xFFFFFFFF);

        graphics.pose().popPose();
    }

    private enum HEART_FILL {
        EMPTY(null),
        HALF("half"),
        FULL("full");

        public final String value;

        HEART_FILL(String value) {
            this.value = value;
        }
    }

    private static HEART_FILL getHeartFill(int i, int hp){
        if(i*2 < hp){
            if(i*2 + 1 == hp){
              return HEART_FILL.HALF;
            }
            return HEART_FILL.FULL;
        }
        return HEART_FILL.EMPTY;
    }

    private static void drawHeart(GuiGraphics graphics, int x, int y, boolean blinking, String blinkingHP, String hp, String absorption){
        graphics.blitSprite(HEARTS_ICONS.get("base" + (blinking ? "_blink" : "")), x, y, ICON_SIZE, ICON_SIZE);
        if(hp != null) {
            if (blinkingHP != null)
                graphics.blitSprite(HEARTS_ICONS.get(hp + "_blink"), x, y, ICON_SIZE, ICON_SIZE);
            graphics.blitSprite(HEARTS_ICONS.get(hp), x, y, ICON_SIZE, ICON_SIZE);
        }
        if(absorption != null)
          graphics.blit(HEARTS_ICONS.get(absorption), x, y, ICON_SIZE, ICON_SIZE, 0, 0, 36, 36, 36, 36);
    }

    private static void drawHPBar(GuiGraphics graphics) {
        Float maxHealth = PlayerHandler.getPlayerMaxHP();
        LivingEntity player = PlayerHandler.getPlayerEntity();
        if(maxHealth == null || player == null) return;

        HealthData.update();
        float currentHealth = HealthData.health();
        float previousHealth = HealthData.previousHealth();
        boolean blinking = HealthData.isBlinking();

        // Variant heart colors
        String heartPrefix = player.hasEffect(MobEffects.POISON) ? "poisoned_"
                : player.hasEffect(MobEffects.WITHER) ? "withered_"
                : "";

        // Low HP Shake
        int tick = player.tickCount;
        boolean lowHealth = Mth.ceil(currentHealth) <= 4;
        SHAKE_ANIM_RANDOM.setSeed(tick * 312871L);

        // Regen effect
        int regenAnimHeartIndex = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regenAnimHeartIndex = tick % Mth.ceil(maxHealth + REGEN_CYCLE_PADDING);
        }

        // TODO: Properly handle Max HP above 20.
        int hearts = Mth.ceil(maxHealth / 2f);
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        int baseY = STARTING_Y + 9;

        RenderSystem.enableBlend();
        for (int i = 0; i < hearts; i++) {
            int localX = STARTING_X + 26 + i * (ICON_SIZE - 1);
            int localY = baseY;
            if (lowHealth) localY += SHAKE_ANIM_RANDOM.nextInt(2);
            if (i == regenAnimHeartIndex) localY -= 2;

            HEART_FILL drawPreviousHP = getHeartFill(i, (int) previousHealth);
            HEART_FILL drawHP = getHeartFill(i, (int) currentHealth);
            HEART_FILL drawAbsorption = getHeartFill(i, absorption);

            drawHeart(graphics, localX, localY, blinking,
                    (drawPreviousHP.value != null ? heartPrefix + drawPreviousHP.value : null),
                    (drawHP.value != null ? heartPrefix + drawHP.value : null),
                    (drawAbsorption.value != null ? "absorbing_" + drawAbsorption.value : null)
                    );
        }
        RenderSystem.disableBlend();
    }
    private static void drawPetHPBar(GuiGraphics graphics, LivingEntity pet){
        float maxHealth = pet.getMaxHealth();
        float currentHealth = pet.getHealth();

        // Low HP Shake
        int tick = pet.tickCount;
        boolean lowHealth = Mth.ceil(currentHealth) <= 4;
        SHAKE_ANIM_RANDOM.setSeed(tick * 312871L);

        int hearts = Mth.ceil(maxHealth / 2f);
        int absorption = Mth.ceil(pet.getAbsorptionAmount());

        int baseY = STARTING_Y + 9 + 27;
        RenderSystem.disableCull(); // Require as we flip the canvas with a negative scale later
        RenderSystem.enableBlend();
        for (int i = 0; i < hearts; i++) {
            int localX = STARTING_X + 26 + 21 + (i % 10) * (ICON_SIZE - 1) + Mth.floor(i / 10f) * 4;
            int localY = baseY + Mth.floor(i / 10f) * (ICON_SIZE - 1);
            if (lowHealth) localY += SHAKE_ANIM_RANDOM.nextInt(2);
            // if (i == regenAnimHeartIndex) localY -= 2;

            HEART_FILL drawHP = getHeartFill(i, (int) currentHealth);
            HEART_FILL drawAbsorption = getHeartFill(i, absorption);

            graphics.pose().pushPose();
            graphics.pose().translate(localX, localY, 0);
            graphics.pose().scale(-1f, 1f, 1f);
            drawHeart(graphics, 0, 0, false,
                    null,
                    (drawHP.value != null ? "vehicle_" + drawHP.value : null),
                    (drawAbsorption.value != null ? "absorbing_" + drawAbsorption.value : null)
            );
            graphics.pose().popPose();
        }
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }
    public static void drawFood(GuiGraphics graphics) {
        Player player = (Player) PlayerHandler.getPlayerEntity();
        if (player == null) return;

        FoodData foodData = player.getFoodData();
        int food = foodData.getFoodLevel();
        String variant = player.hasEffect(MobEffects.HUNGER) ? "_hunger" : "";

        int tick = player.tickCount;
        boolean shaking = foodData.getSaturationLevel() <= 0f && tick % (food * 3 + 1) == 0;
        SHAKE_ANIM_RANDOM.setSeed(tick * 4517L);

        int baseY = STARTING_Y + 19;

        RenderSystem.disableCull(); // Require as we flip the canvas with a negative scale later
        RenderSystem.enableBlend();
        for (int i = 0; i < 10; i++) {
            int localX = STARTING_X + 26 + i * (ICON_SIZE - 1) + ICON_SIZE;
            int localY = baseY;
            if (shaking) localY += SHAKE_ANIM_RANDOM.nextInt(3) - 1;
            graphics.pose().pushPose();
            graphics.pose().translate(localX, localY, 0);
            graphics.pose().scale(-1f, 1f, 1f);
            graphics.blitSprite(FOOD_ICONS.get("empty" + variant), 0, 0, ICON_SIZE, ICON_SIZE);

            if (i * 2 + 1 < food) {
                graphics.blitSprite(FOOD_ICONS.get("full" + variant), 0, 0, ICON_SIZE, ICON_SIZE);
            } else if (i * 2 + 1 == food) {
                graphics.blitSprite(FOOD_ICONS.get("half" + variant), 0, 0, ICON_SIZE, ICON_SIZE);
            }
            graphics.pose().popPose();
        }
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public static int drawEffect(GuiGraphics graphics, List<MobEffectInstance> effects, int limit, int offsetIndex){
        MobEffectInstance effect;
        int localX = STARTING_X + 102;
        int localY = STARTING_Y + 10;
        int i = 0;

        for (; i + offsetIndex < limit && i < effects.size(); i++) {
            // Grab the effect and remove it from the list
            effect = effects.getFirst();
            effects.removeFirst();

            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect());
            Font font = Minecraft.getInstance().font;
            int spacing = (11 * (i + offsetIndex));

            RenderSystem.enableBlend();
            graphics.blit(localX + spacing, localY, 0, ICON_SIZE, ICON_SIZE, sprite);
            RenderSystem.disableBlend();

            String text = TimeTextFormatter.ticksToCompactTimeString(effect.isInfiniteDuration() ? -1 : effect.getDuration());
            float scale = 0.5f;
            float width = font.width(text) * scale;

            graphics.pose().pushPose();
            graphics.pose().translate(localX + (ICON_SIZE/2.0f) - (width/2.0f) + spacing, localY + 2 + ICON_SIZE, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.drawString(font, text, 0, 0, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
        return i;
    }

    public static void drawEffects(GuiGraphics graphics) {
        Player player = (Player) PlayerHandler.getPlayerEntity();
        if (player == null) return;

        Iterator<MobEffectInstance> effects = player.getActiveEffects().iterator();
        List<MobEffectInstance> buffs = new ArrayList<>();
        List<MobEffectInstance> debuffs = new ArrayList<>();
        List<MobEffectInstance> neutrals = new ArrayList<>();

        // Priority effect holders
        MobEffectInstance absorption = null;
        MobEffectInstance poison = null;
        MobEffectInstance wither = null;

        if(!effects.hasNext()) return;
        MobEffectInstance effect;

        do{
            effect = effects.next();
            if(!effect.showIcon()) continue;
            if(effect.isInfiniteDuration()) continue;

            // Special cases
            if(effect.getEffect() == MobEffects.ABSORPTION){
                absorption = effect; continue;
            }
            if(effect.getEffect() == MobEffects.POISON){
                poison = effect; continue;
            }
            if(effect.getEffect() == MobEffects.WITHER){
                wither = effect; continue;
            }

            // Regular effects
            switch (effect.getEffect().value().getCategory()){
                case BENEFICIAL :
                    buffs.add(effect); continue;
                case HARMFUL:
                    debuffs.add(effect); continue;
                case NEUTRAL:
                    neutrals.add(effect);
            }
        } while(effects.hasNext());

        if(absorption != null) buffs.addFirst(absorption);
        if(wither != null) debuffs.addFirst(wither); // Wither first is added first so Poison shows before it
        if(poison != null) debuffs.addFirst(poison);

        int drawnEffects = 0;
        if(!debuffs.isEmpty())
            drawnEffects += drawEffect(graphics, debuffs, DEBUFF_PRIORITY_SLOTS, drawnEffects);
        if(!buffs.isEmpty())
            drawnEffects += drawEffect(graphics, buffs, DEBUFF_PRIORITY_SLOTS + BUFF_PRIORITY_SLOTS, drawnEffects);
        if(!neutrals.isEmpty())
            drawnEffects += drawEffect(graphics, neutrals, MAX_EFFECTS, drawnEffects);

        // If there is leftover space
        drawnEffects += drawEffect(graphics, debuffs, MAX_EFFECTS, drawnEffects);
        drawnEffects += drawEffect(graphics, buffs, MAX_EFFECTS, drawnEffects);
        drawEffect(graphics, neutrals, MAX_EFFECTS, drawnEffects);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { HealthData.reset(); }
    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) { HealthData.reset(); }
    @SubscribeEvent
    public static void onRespawn(ClientPlayerNetworkEvent.Clone event) { HealthData.reset(); }
}