package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;

import java.util.List;

public record ManyFinder(List<RuleFinder> finders) implements RuleFinder {
    public static final MapCodec<ManyFinder> CODEC = RuleFinder.CODEC.listOf().fieldOf("finders").xmap(ManyFinder::new, ManyFinder::finders);

    @Override
    public ModificationView find() {
        return (c, m, source) -> {
            var s = source;
            for (var finder : finders) {
                s = finder.find().apply(c, m, s);
            }
            return s;
        };
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
