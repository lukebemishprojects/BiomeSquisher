package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record MatchingModifier(RulePredicate predicate, RuleModifier modifier) implements RuleModifier {
    public static final MapCodec<MatchingModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RulePredicate.CODEC.fieldOf("predicate").forGetter(MatchingModifier::predicate),
        RuleModifier.CODEC.fieldOf("modifier").forGetter(MatchingModifier::modifier)
    ).apply(i, MatchingModifier::new));

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        if (SurfaceRuleModifierUtils.isSequence(source)) {
            List<SurfaceRules.RuleSource> sources = SurfaceRuleModifierUtils.sequence(source).stream().map(s -> predicate.matches(context, s) ? modifier.apply(context, s) : s).toList();
            return SurfaceRuleModifierUtils.createSequence(sources);
        } else {
            SurfaceRuleModifierUtils.warnOnNonSequence(context, source);
        }
        return source;
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return CODEC;
    }
}
