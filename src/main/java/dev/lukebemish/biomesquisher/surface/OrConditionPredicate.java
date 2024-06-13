package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record OrConditionPredicate(List<ConditionPredicate> predicates) implements ConditionPredicate {
    public static final MapCodec<OrConditionPredicate> CODEC = ConditionPredicate.CODEC.listOf().fieldOf("predicates").xmap(OrConditionPredicate::new, OrConditionPredicate::predicates);

    @Override
    public MapCodec<OrConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        return predicates.stream().anyMatch(p -> p.matches(context, source));
    }
}
