package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.impl.Utils;

public record CheckFinder(RulePredicate predicate) implements RuleFinder {
    public static final MapCodec<CheckFinder> CODEC = RulePredicate.CODEC.fieldOf("predicate").xmap(CheckFinder::new, CheckFinder::predicate);

    @Override
    public ModificationView find() {
        return (c, m, source) -> {
            if (predicate.matches(c, source)) {
                return m.apply(c, source);
            } else {
                Utils.LOGGER.warn("In surface rule modifier {} did not match predicate {}", c.modifierKey(), predicate);
            }
            return source;
        };
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
