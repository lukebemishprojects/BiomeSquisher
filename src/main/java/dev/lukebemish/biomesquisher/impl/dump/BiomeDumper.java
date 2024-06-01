package dev.lukebemish.biomesquisher.impl.dump;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.Dimension;
import dev.lukebemish.biomesquisher.impl.mixin.MultiNoiseBiomeSourceAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;

public class BiomeDumper {
    public static final boolean IS_PNGJ_PRESENT = isPngjPresent();

    private static boolean isPngjPresent() {
        try {
            Class.forName("ar.com.hjg.pngj.PngWriter", false, BiomeDumper.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static final String[] EXAMPLES = Arrays.stream(dev.lukebemish.biomesquisher.impl.Dimension.values()).map(v -> v.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);

    public record SliceLocation(float i, float j, float k, float l) {
        public static final Codec<SliceLocation> CODEC = Codec.FLOAT.listOf().comapFlatMap(
            l -> l.size() == 4 ?
                DataResult.success(new SliceLocation(l.get(0), l.get(1), l.get(2), l.get(3))) :
                DataResult.error(() -> "Slice location must have 4 elements"),
            s -> List.of(s.i, s.j, s.j, s.l)
        );

        public long getAt(int index) {
            float val = switch (index) {
                case 0 -> i;
                case 1 -> j;
                case 2 -> k;
                case 3 -> l;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
            return Climate.quantizeCoord(val);
        }
    }

    public record SliceFrame(float xMin, float xMax, float yMin, float yMax) {
        public static final Codec<SliceFrame> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("x_min").forGetter(SliceFrame::xMin),
            Codec.FLOAT.fieldOf("x_max").forGetter(SliceFrame::xMax),
            Codec.FLOAT.fieldOf("y_min").forGetter(SliceFrame::yMin),
            Codec.FLOAT.fieldOf("y_max").forGetter(SliceFrame::yMax)
        ).apply(i, SliceFrame::new));
    }

    public static void dumpPng(Level level, MultiNoiseBiomeSource source, Dimension x, Dimension y, SliceLocation location, SliceFrame frame) throws IOException {
        Output output;
        if (IS_PNGJ_PRESENT) {
            output = PngOutput.INSTANCE_1024;
        } else {
            output = (l, biomeGetter, possibleBiomes) -> {
                throw new IOException("PNGJ is not present; cannot export biome dump as PNG!");
            };
        }
        dump(level, source, x, y, location, output, frame);
    }

    public static void dump(Level level, MultiNoiseBiomeSource source, Dimension x, Dimension y, SliceLocation location, Output output, SliceFrame frame) throws IOException {
        Climate.ParameterList<Holder<Biome>> parameters = ((MultiNoiseBiomeSourceAccessor) source).biomesquisher_parameters();
        long[] indices = new long[6];
        int indexed = 0;
        for (int i = 0; i < 6; i++) {
            if (i == x.index() || i == y.index()) {
                continue;
            }
            indices[i] = location.getAt(indexed);
            indexed += 1;
        }
        output.dump(level, (xF, yF) -> {
            indices[x.index()] = Climate.quantizeCoord((xF * (frame.xMax - frame.xMin)) + frame.xMin);
            indices[y.index()] = Climate.quantizeCoord((yF * (frame.yMax - frame.yMin)) + frame.yMin);
            return parameters.findValue(new Climate.TargetPoint(
                indices[0], indices[1], indices[2], indices[3], indices[4], indices[5]
            ));
        }, source.possibleBiomes());
    }

    @FunctionalInterface
    public interface Output {

        void dump(Level level, BiFunction<Float, Float, Holder<Biome>> biomeGetter, Set<Holder<Biome>> possibleBiomes) throws IOException;
    }

    public static int hashBiome(ResourceKey<Biome> key) {
        return (key.location().hashCode() & 0xFFFFFF) | 0xFF000000;
    }
}
