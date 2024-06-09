package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record AndPredicate(List<RulePredicate> predicates) implements RulePredicate {
    public static final MapCodec<AndPredicate> CODEC = RulePredicate.CODEC.listOf().fieldOf("predicates").xmap(AndPredicate::new, AndPredicate::predicates);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        return predicates.stream().allMatch(p -> p.matches(context, ruleSource));
    }
}
