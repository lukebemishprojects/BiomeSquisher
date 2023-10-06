package dev.lukebemish.biomesquisher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public sealed interface DimensionBehaviour<T extends DimensionBehaviour<T>> {
    @Contract(pure = true)
    default @Nullable Range asRange() {
        return null;
    }

    @Contract(pure = true)
    default @Nullable Squish asSquish() {
        return null;
    }

    T self();

    float center();

    final class Range implements DimensionBehaviour<Range> {
        private final float min;
        private final float max;

        public Range(float min, float max) {
            this.min = min;
            this.max = max;
            if (min >= max) throw new IllegalArgumentException("min must be less than max");
        }

        public float min() { return min; }
        public float max() { return max; }

        @Override
        public Range asRange() {
            return this;
        }

        @Override
        public Range self() {
            return this;
        }

        @Override
        public float center() {
            return (min + max) / 2f;
        }

        @Override
        public String toString() {
            return "DimensionBehaviour.Range{" +
                "min=" + min +
                ", max=" + max +
                '}';
        }
    }

    final class Squish implements DimensionBehaviour<Squish> {
        private final float position;

        public Squish(float position) {
            this.position = position;
        }

        public float position() { return position; }

        @Override
        public Squish asSquish() {
            return this;
        }

        @Override
        public Squish self() {
            return this;
        }

        @Override
        public float center() {
            return position;
        }

        @Override
        public String toString() {
            return "DimensionBehaviour.Squish{" +
                "position=" + position +
                '}';
        }
    }
}
