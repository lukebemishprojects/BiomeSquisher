package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record ReplaceConditionModifier(SurfaceRules.ConditionSource condition) implements RuleModifier {
    public static final MapCodec<ReplaceConditionModifier> CODEC = SurfaceRules.ConditionSource.CODEC.fieldOf("condition").xmap(ReplaceConditionModifier::new, ReplaceConditionModifier::condition);

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        if (SurfaceRuleModifierUtils.isTest(source)) {
            var thenRun = SurfaceRuleModifierUtils.thenRun(source);
            return SurfaceRules.ifTrue(condition, thenRun);
        } else {
            SurfaceRuleModifierUtils.warnOnNonTest(context, source);
        }
        return source;
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return CODEC;
    }
}
