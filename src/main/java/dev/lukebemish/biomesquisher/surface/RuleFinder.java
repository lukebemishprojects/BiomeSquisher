package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.Function;

public interface RuleFinder {
    Codec<RuleFinder> CODEC = BiomeSquisherRegistries.SURFACE_FINDER_TYPES.byNameCodec()
        .dispatch(RuleFinder::codec, Function.identity());

    ModifierTarget find(SurfaceRules.RuleSource source);

    MapCodec<? extends RuleFinder> codec();
}
