package dev.lukebemish.biomesquisher;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lukebemish.biomesquisher.impl.Dimension;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class Relative {
    public static final Codec<Relative> CODEC = Codec.unboundedMap(Dimension.CODEC, Position.CODEC).xmap(Relative::of, Relative::positions).flatXmap(Relative::verify, DataResult::success);

    private final Map<Dimension, Position> positions;

    public static Relative of(Map<Dimension, Position> map) {
        var result = verify(new Relative(map));
        if (result.error().isPresent()) {
            throw new IllegalArgumentException(result.error().get().message());
        }
        //noinspection OptionalGetWithoutIsPresent
        return result.result().get();
    }

    private static DataResult<Relative> verify(Relative relative) {
        boolean hasNonCenter = false;
        for (Dimension dimension : Dimension.values()) {
            Position position = relative.positions.get(dimension);
            if (position != null && !dimension.squish()) {
                return DataResult.error(() -> "Dimension " + dimension + " is not squishable, but was given a position in a relative.");
            } else if (position != null && position != Position.CENTER) {
                hasNonCenter = true;
            }
        }
        if (!hasNonCenter) {
            return DataResult.error(() -> "Relative must have at least one non-center position.");
        }
        return DataResult.success(relative);
    }

    private Relative(Map<Dimension, Position> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Dimension dimension : Dimension.SQUISH) {
            builder.append(dimension.getSerializedName()).append('=').append(positions.getOrDefault(dimension, Position.CENTER).getSerializedName()).append(", ");
        }

        return "Relative{" + builder.substring(0, builder.length() - 2) + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Relative relative = (Relative) object;
        return Objects.equals(positions, relative.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions);
    }

    public Map<Dimension, Position> positions() {
        return positions;
    }

    public enum Position implements StringRepresentable {
        START(-1),
        CENTER(0),
        END(1);

        public static final Codec<Position> CODEC = StringRepresentable.fromEnum(Position::values);
        private final float offset;

        Position(float offset) {
            this.offset = offset;
        }

        public float offset() {
            return offset;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Relative DEFAULT = of(ImmutableMap.<Dimension,Position>builder()
        .put(Dimension.TEMPERATURE, Position.START)
        .build()
    );
}
