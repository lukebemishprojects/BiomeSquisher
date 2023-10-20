package dev.lukebemish.biomesquisher.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

public record InternalScalingSampler(DensityFunction input, float scale) implements DensityFunction {
    public static final MapCodec<InternalScalingSampler> DATA_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(InternalScalingSampler::input),
        Codec.FLOAT.fieldOf("scale").forGetter(InternalScalingSampler::scale)
    ).apply(i, InternalScalingSampler::new));
    public static final KeyDispatchDataCodec<InternalScalingSampler> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    @Override
    public double compute(FunctionContext context) {
        FunctionContext scaled = new FunctionContext() {
            @Override
            public int blockX() {
                return Math.round(context.blockX() * scale);
            }

            @Override
            public int blockY() {
                return context.blockY();
            }

            @Override
            public int blockZ() {
                return Math.round(context.blockZ() * scale);
            }

            @Override
            public @NotNull Blender getBlender() {
                return context.getBlender();
            }
        };
        return input.compute(scaled);
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @Override
    public @NotNull DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new InternalScalingSampler(input.mapAll(visitor), scale));
    }

    @Override
    public double minValue() {
        return input.minValue();
    }

    @Override
    public double maxValue() {
        return input.maxValue();
    }

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    public static class SetScale implements Visitor {
        private boolean scaled = false;
        private final float scale;

        public SetScale(float scale) {
            this.scale = scale;
        }

        @Override
        public @NotNull DensityFunction apply(DensityFunction densityFunction) {
            if (densityFunction instanceof InternalScalingSampler scaling) {
                if (scaled) {
                    return apply(scaling.input());
                }
                scaled = true;
                return new InternalScalingSampler(apply(scaling.input()), scaling.scale() * scale);
            }
            return densityFunction;
        }

        public boolean scaled() {
            return scaled;
        }

        @Override
        public @NotNull NoiseHolder visitNoise(NoiseHolder noiseHolder) {
            return Visitor.super.visitNoise(noiseHolder);
        }
    }
}
