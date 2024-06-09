package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.Function;

public interface RulePredicate {
    Codec<RulePredicate> CODEC = BiomeSquisherRegistries.SURFACE_PREDICATE_TYPES.byNameCodec()
        .dispatch(RulePredicate::codec, Function.identity());

    MapCodec<? extends RulePredicate> codec();

    boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource);
}
