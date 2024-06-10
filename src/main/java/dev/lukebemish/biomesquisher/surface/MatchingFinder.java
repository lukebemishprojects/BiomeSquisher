package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public record MatchingFinder(RulePredicate predicate) implements RuleFinder {
    public static final MapCodec<MatchingFinder> CODEC = RulePredicate.CODEC.fieldOf("predicate").xmap(MatchingFinder::new, MatchingFinder::predicate);

    @Override
    public ModificationView find() {
        return (c, m, source) -> {
            if (SurfaceRuleModifierUtils.isSequence(source)) {
                List<SurfaceRules.RuleSource> parts = SurfaceRuleModifierUtils.sequence(source);
                List<SurfaceRules.RuleSource> newParts = new ArrayList<>();
                for (var part : parts) {
                    if (predicate.matches(c, part)) {
                        newParts.add(m.apply(c, part));
                    } else {
                        newParts.add(part);
                    }
                }
                return SurfaceRuleModifierUtils.createSequence(newParts);
            } else {
                SurfaceRuleModifierUtils.warnOnNonSequence(c, source);
            }
            return source;
        };
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
