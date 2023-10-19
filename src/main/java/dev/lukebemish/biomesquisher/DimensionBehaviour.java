package dev.lukebemish.biomesquisher;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public sealed interface DimensionBehaviour {
    BiMap<Type, Codec<? extends DimensionBehaviour>> TYPE_MAP = ImmutableBiMap.<Type, Codec<? extends DimensionBehaviour>>builder()
        .put(Type.RANGE, Range.CODEC)
        .put(Type.SQUISH, Squish.CODEC)
        .build();
    Codec<DimensionBehaviour> CODEC = Type.CODEC.dispatch(b -> TYPE_MAP.inverse().get(b.codec()), TYPE_MAP::get);
    @Contract(pure = true)
    default @Nullable Range asRange() {
        return null;
    }

    @Contract(pure = true)
    default @Nullable Squish asSquish() {
        return null;
    }

    double center();
    Codec<? extends DimensionBehaviour> codec();

    enum Type implements StringRepresentable {
        RANGE,
        SQUISH;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    final class Range implements DimensionBehaviour {
        public static final Codec<Range> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("min").forGetter(Range::min),
            Codec.DOUBLE.fieldOf("max").forGetter(Range::max)
        ).apply(i, Range::new));

        private final double min;
        private final double max;

        public Range(double min, double max) {
            this.min = min;
            this.max = max;
            if (min >= max) throw new IllegalArgumentException("min must be less than max");
        }

        public double min() {
            return min;
        }

        public double max() {
            return max;
        }

        @Override
        public Range asRange() {
            return this;
        }

        @Override
        public double center() {
            return (min + max) / 2f;
        }

        @Override
        public Codec<? extends DimensionBehaviour> codec() {
            return CODEC;
        }

        @Override
        public String toString() {
            return "DimensionBehaviour.Range{" +
                "min=" + min +
                ", max=" + max +
                '}';
        }
    }

    final class Squish implements DimensionBehaviour {
        public static final Codec<Squish> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("position").forGetter(Squish::position),
            Codec.DOUBLE.optionalFieldOf("degree", 1d).forGetter(Squish::degree)
        ).apply(i, Squish::new));

        private final double position;
        private final double degree;

        public Squish(double position, double degree) {
            this.position = position;
            this.degree = degree;
        }

        public double position() {
            return position;
        }

        @Override
        public Squish asSquish() {
            return this;
        }

        @Override
        public double center() {
            return position;
        }

        @Override
        public Codec<? extends DimensionBehaviour> codec() {
            return CODEC;
        }

        @Override
        public String toString() {
            return "DimensionBehaviour.Squish{" +
                "position=" + position +
                ", degree=" + degree +
                '}';
        }

        public double degree() {
            return degree;
        }
    }
}
