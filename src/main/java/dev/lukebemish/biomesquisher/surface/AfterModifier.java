package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public record AfterModifier(RulePredicate predicate, SurfaceRules.RuleSource source) implements RuleModifier {
    public static final MapCodec<AfterModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RulePredicate.CODEC.fieldOf("predicate").forGetter(AfterModifier::predicate),
        SurfaceRules.RuleSource.CODEC.fieldOf("source").forGetter(AfterModifier::source)
    ).apply(i, AfterModifier::new));

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        if (SurfaceRuleModifierUtils.isSequence(source)) {
            List<SurfaceRules.RuleSource> sources = new ArrayList<>();
            boolean found = false;
            for (SurfaceRules.RuleSource s : SurfaceRuleModifierUtils.sequence(source)) {
                sources.add(s);
                if (!found && predicate.matches(context, s)) {
                    found = true;
                    sources.add(source);
                }
            }
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
