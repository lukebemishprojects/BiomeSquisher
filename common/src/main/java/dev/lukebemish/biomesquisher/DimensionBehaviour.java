package dev.lukebemish.biomesquisher;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.Context;
import dev.lukebemish.biomesquisher.impl.Dimension;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public sealed interface DimensionBehaviour {
    BiMap<Type, Codec<? extends DimensionBehaviour>> TYPE_MAP = ImmutableBiMap.<Type, Codec<? extends DimensionBehaviour>>builder()
        .put(Type.RANGE, Range.CODEC)
        .put(Type.SQUISH, Squish.CODEC)
        .build();
    Codec<DimensionBehaviour> CODEC = Type.CODEC.dispatch(b -> TYPE_MAP.inverse().get(b.codec()), TYPE_MAP::get);
    @Contract(pure = true)
    default Range asRange() {
        throw new UnsupportedOperationException();
    }

    default boolean isRange() {
        return false;
    }

    @Contract(pure = true)
    default Squish asSquish() {
        throw new UnsupportedOperationException();
    }

    default boolean isSquish() {
        return false;
    }

    double globalCenter();

    @ApiStatus.Internal
    default double center(Context context, Dimension dimension) {
        return Utils.recontext(globalCenter(), context, dimension);
    }

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
            Codec.DOUBLE.fieldOf("min").forGetter(Range::globalMin),
            Codec.DOUBLE.fieldOf("max").forGetter(Range::globalMax)
        ).apply(i, Range::new));

        private final double min;
        private final double max;

        public Range(double min, double max) {
            this.min = min;
            this.max = max;
            if (min >= max) throw new IllegalArgumentException("min must be less than max");
        }

        public double globalMin() {
            return min;
        }

        public double globalMax() {
            return max;
        }

        @ApiStatus.Internal
        public double min(Context context, Dimension dimension) {
            return Utils.recontext(min, context, dimension);
        }

        @ApiStatus.Internal
        public double max(Context context, Dimension dimension) {
            return Utils.recontext(max, context, dimension);
        }

        @Override
        public Range asRange() {
            return this;
        }

        @Override
        public boolean isRange() {
            return true;
        }

        @Override
        public double globalCenter() {
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
            Codec.DOUBLE.fieldOf("position").forGetter(Squish::globalPosition),
            Codec.DOUBLE.optionalFieldOf("degree", 1d).forGetter(Squish::degree)
        ).apply(i, Squish::new));

        private final double position;
        private final double degree;

        public Squish(double position, double degree) {
            this.position = position;
            this.degree = degree;
        }

        public double globalPosition() {
            return position;
        }

        @ApiStatus.Internal
        public double position(Context context, Dimension dimension) {
            return Utils.recontext(position, context, dimension);
        }

        @Override
        public Squish asSquish() {
            return this;
        }

        @Override
        public boolean isSquish() {
            return true;
        }

        @Override
        public double globalCenter() {
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
