package io.github.aegonlegends.legendsrpghud.utils;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class EffectHelper {
    private EffectHelper() {
        /* This utility class should not be instantiated */
    }

    public record EffectLists(
            List<MobEffectInstance> buffs,
            List<MobEffectInstance> debuffs,
            List<MobEffectInstance> neutrals) {

        public EffectLists {
            buffs = List.copyOf(buffs);
            debuffs = List.copyOf(debuffs);
            neutrals = List.copyOf(neutrals);
        }
    }

    public static EffectLists getEffects(LivingEntity entity) {
        List<MobEffectInstance> buffs = new ArrayList<>();
        List<MobEffectInstance> debuffs = new ArrayList<>();
        List<MobEffectInstance> neutrals = new ArrayList<>();

        if (entity == null) return null;

        Iterator<MobEffectInstance> effects = entity.getActiveEffects().iterator();

        if(effects.hasNext()) {
            MobEffectInstance effect;

            do {
                effect = effects.next();
                if (!effect.showIcon()) continue;

                switch (effect.getEffect().value().getCategory()) {
                    case BENEFICIAL:
                        buffs.add(effect);
                        break;
                    case HARMFUL:
                        debuffs.add(effect);
                        break;
                    case NEUTRAL:
                        neutrals.add(effect);
                }
            } while (effects.hasNext());

            buffs.sort(EffectHelper.EFFECT_COMPARATOR);
            debuffs.sort(EffectHelper.EFFECT_COMPARATOR);
            neutrals.sort(EffectHelper.EFFECT_COMPARATOR);
        }

        return new EffectLists(buffs, debuffs, neutrals);
    }

    public static final Comparator<MobEffectInstance> EFFECT_COMPARATOR = (a, b) -> {
        // If either is infinite duration, compare infinities
        if(a.isInfiniteDuration() || b.isInfiniteDuration()) {
            int result = 0;
            if(a.isInfiniteDuration()) result = -1;
            if(b.isInfiniteDuration()) result += 1;
            return result;
        }
        return Integer.compare(a.getDuration(), b.getDuration());
    };

}
