package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record ManyFinder(List<RuleFinder> finders) implements RuleFinder {
    public static final MapCodec<ManyFinder> CODEC = RuleFinder.CODEC.listOf().fieldOf("finders").xmap(ManyFinder::new, ManyFinder::finders);

    @Override
    public ModifierTarget find(SurfaceRules.RuleSource source) {
        return (c, m) -> {
            var s = source;
            for (var finder : finders) {
                s = finder.find(s).apply(c, m);
            }
            return s;
        };
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
