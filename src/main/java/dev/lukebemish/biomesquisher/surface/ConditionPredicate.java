package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.Function;

public interface ConditionPredicate {
    Codec<ConditionPredicate> CODEC = BiomeSquisherRegistries.SURFACE_CONDITION_PREDICATE_TYPES.byNameCodec()
        .dispatch(ConditionPredicate::codec, Function.identity());

    MapCodec<? extends ConditionPredicate> codec();

    boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source);
}
