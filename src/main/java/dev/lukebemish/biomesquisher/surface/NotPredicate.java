package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record NotPredicate(RulePredicate predicate) implements RulePredicate {
    public static final MapCodec<NotPredicate> CODEC = RulePredicate.CODEC.fieldOf("predicate").xmap(NotPredicate::new, NotPredicate::predicate);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        return !predicate.matches(context, ruleSource);
    }
}
