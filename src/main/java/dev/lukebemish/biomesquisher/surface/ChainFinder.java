package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;

import java.util.List;

public record ChainFinder(List<RuleFinder> finders) implements RuleFinder {
    public static final MapCodec<ChainFinder> CODEC = RuleFinder.CODEC.listOf().fieldOf("finders").xmap(ChainFinder::new, ChainFinder::finders);

    @Override
    public ModificationView find() {
        ModificationView view = ModificationView.simple();
        for (RuleFinder finder : finders) {
            var oldView = view;
            view = (c, m, source) -> oldView.apply(c, (c1, s) -> finder.find().apply(c1, m, s), source);
        }
        return view;
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
