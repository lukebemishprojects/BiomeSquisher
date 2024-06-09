package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record AnyPredicate(RulePredicate predicate) implements RulePredicate {
    public static final MapCodec<AnyPredicate> CODEC = RulePredicate.CODEC.fieldOf("predicate").xmap(AnyPredicate::new, AnyPredicate::predicate);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        if (SurfaceRuleModifierUtils.isSequence(ruleSource)) {
            return SurfaceRuleModifierUtils.sequence(ruleSource).stream().anyMatch(s -> predicate.matches(context, s));
        } else {
            SurfaceRuleModifierUtils.warnOnNonSequence(context, ruleSource);
        }
        return false;
    }
}
