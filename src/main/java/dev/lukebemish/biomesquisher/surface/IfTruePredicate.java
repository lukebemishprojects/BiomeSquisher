package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record IfTruePredicate(ConditionPredicate predicate) implements RulePredicate {
    public static final MapCodec<IfTruePredicate> CODEC = ConditionPredicate.CODEC.fieldOf("predicate").xmap(IfTruePredicate::new, IfTruePredicate::predicate);

    @Override
    public MapCodec<IfTruePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        if (SurfaceRuleModifierUtils.isTest(ruleSource)) {
            var ifTrue = SurfaceRuleModifierUtils.ifTrue(ruleSource);
            return predicate.matches(context, ifTrue);
        } else {
            SurfaceRuleModifierUtils.warnOnNonTest(context, ruleSource);
        }
        return false;
    }
}
