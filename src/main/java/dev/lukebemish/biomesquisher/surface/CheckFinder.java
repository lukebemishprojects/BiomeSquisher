package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record CheckFinder(RulePredicate predicate) implements RuleFinder {
    public static final MapCodec<CheckFinder> CODEC = RulePredicate.CODEC.fieldOf("predicate").xmap(CheckFinder::new, CheckFinder::predicate);

    @Override
    public ModifierTarget find(SurfaceRules.RuleSource source) {
        return (c, m) -> predicate.matches(c, source) ? m.apply(c, source) : source;
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
