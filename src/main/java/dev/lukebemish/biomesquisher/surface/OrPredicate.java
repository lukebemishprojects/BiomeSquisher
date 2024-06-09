package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record OrPredicate(List<RulePredicate> predicates) implements RulePredicate {
    public static final MapCodec<OrPredicate> CODEC = RulePredicate.CODEC.listOf().fieldOf("predicates").xmap(OrPredicate::new, OrPredicate::predicates);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        return predicates.stream().anyMatch(p -> p.matches(context, ruleSource));
    }
}
