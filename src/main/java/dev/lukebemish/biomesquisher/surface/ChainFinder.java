package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record ChainFinder(List<RuleFinder> finders) implements RuleFinder {
    public static final MapCodec<ChainFinder> CODEC = RuleFinder.CODEC.listOf().fieldOf("finders").xmap(ChainFinder::new, ChainFinder::finders);

    @Override
    public ModifierTarget find(SurfaceRules.RuleSource source) {
        ModifierTarget view = ModifierTarget.simple(source);
        for (RuleFinder finder : finders) {
            var oldView = view;
            view = (c, m) -> oldView.apply(c, (c1, s) -> finder.find(s).apply(c1, m));
        }
        return view;
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
