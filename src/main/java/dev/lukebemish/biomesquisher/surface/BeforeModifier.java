package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public record BeforeModifier(RulePredicate predicate, SurfaceRules.RuleSource source) implements RuleModifier {
    public static final MapCodec<BeforeModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RulePredicate.CODEC.fieldOf("predicate").forGetter(BeforeModifier::predicate),
        SurfaceRules.RuleSource.CODEC.fieldOf("source").forGetter(BeforeModifier::source)
    ).apply(i, BeforeModifier::new));

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        if (SurfaceRuleModifierUtils.isSequence(source)) {
            List<SurfaceRules.RuleSource> sources = new ArrayList<>();
            boolean found = false;
            for (SurfaceRules.RuleSource s : SurfaceRuleModifierUtils.sequence(source)) {
                if (!found && predicate.matches(context, s)) {
                    found = true;
                    sources.add(source);
                }
                sources.add(s);
            }
            if (!found) {
                Utils.LOGGER.warn("In surface rule modifier {}, predicate {} did not match any rules", this, predicate);
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
