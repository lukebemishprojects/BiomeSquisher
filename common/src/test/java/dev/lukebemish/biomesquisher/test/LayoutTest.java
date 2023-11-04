package dev.lukebemish.biomesquisher.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.Dimension;
import dev.lukebemish.biomesquisher.impl.dump.BiomeDumper;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public record LayoutTest(ResourceLocation location, LayoutSpecs specs, Layout target) {
    public record LayoutSpecs(Dimension x, Dimension y, BiomeDumper.SliceLocation slice, BiomeDumper.SliceFrame frame) {
        public static final Codec<LayoutSpecs> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Dimension.CODEC.fieldOf("x").forGetter(LayoutSpecs::x),
            Dimension.CODEC.fieldOf("y").forGetter(LayoutSpecs::y),
            BiomeDumper.SliceLocation.CODEC.fieldOf("slice").forGetter(LayoutSpecs::slice),
            BiomeDumper.SliceFrame.CODEC.fieldOf("frame").forGetter(LayoutSpecs::frame)
        ).apply(instance, LayoutSpecs::new));
    }

    public static final class Layout {
        private final int[][] data;

        public Layout(int[][] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Layout layout)) return false;
            return Arrays.deepEquals(data, layout.data);
        }
    }
}
