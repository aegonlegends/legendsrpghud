package io.github.aegonlegends.legendsrpghud;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(value = LegendsRPGHUDClient.MODID, dist = Dist.CLIENT)
public class LegendsRPGHUDClient {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "legendsrpghud";

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public LegendsRPGHUDClient(IEventBus modEventBus, ModContainer modContainer) {

    }
}

