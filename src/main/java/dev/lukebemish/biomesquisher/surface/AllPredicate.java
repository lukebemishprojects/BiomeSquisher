package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record AllPredicate(RulePredicate predicate) implements RulePredicate {
    public static final MapCodec<AllPredicate> CODEC = RulePredicate.CODEC.fieldOf("predicate").xmap(AllPredicate::new, AllPredicate::predicate);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        if (SurfaceRuleModifierUtils.isSequence(ruleSource)) {
            return SurfaceRuleModifierUtils.sequence(ruleSource).stream().allMatch(s -> predicate.matches(context, s));
        } else {
            SurfaceRuleModifierUtils.warnOnNonSequence(context, ruleSource);
        }
        return false;
    }
}
