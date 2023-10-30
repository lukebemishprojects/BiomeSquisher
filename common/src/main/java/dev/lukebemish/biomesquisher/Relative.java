package dev.lukebemish.biomesquisher;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public final class Relative {
    public static final Codec<Relative> CODEC = RecordCodecBuilder.create(i -> i.group(
        Position.CODEC.optionalFieldOf("temperature", Position.CENTER).forGetter(Relative::temperature),
        Position.CODEC.optionalFieldOf("humidity", Position.CENTER).forGetter(Relative::humidity),
        Position.CODEC.optionalFieldOf("erosion", Position.CENTER).forGetter(Relative::erosion),
        Position.CODEC.optionalFieldOf("weirdness", Position.CENTER).forGetter(Relative::weirdness)
    ).apply(i, Relative::new));

    private final Position temperature;
    private final Position humidity;
    private final Position erosion;
    private final Position weirdness;

    public Relative(Position temperature, Position humidity, Position erosion, Position weirdness) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.erosion = erosion;
        this.weirdness = weirdness;
    }

    public Position temperature() {
        return temperature;
    }

    public Position humidity() {
        return humidity;
    }

    public Position erosion() {
        return erosion;
    }

    public Position weirdness() {
        return weirdness;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Relative relative) {
            return Objects.equals(this.temperature, relative.temperature) &&
                Objects.equals(this.humidity, relative.humidity) &&
                Objects.equals(this.erosion, relative.erosion) &&
                Objects.equals(this.weirdness, relative.weirdness);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, humidity, erosion, weirdness);
    }

    @Override
    public String toString() {
        return "Relative{" +
                "temperature=" + temperature +
                ", humidity=" + humidity +
                ", erosion=" + erosion +
                ", weirdness=" + weirdness +
            '}';
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

    public static final class Series {
        private final List<Relative> relatives;

        public static final Codec<Series> CODEC = Relative.CODEC.listOf().comapFlatMap(Series::validate, Function.identity()).xmap(Series::new, Series::relatives);

        public Series(List<Relative> relatives) {
            var result = validate(relatives);
            if (result.error().isPresent()) {
                throw new IllegalArgumentException(result.error().get().message());
            }
            //noinspection OptionalGetWithoutIsPresent
            this.relatives = result.result().get();
        }

        public List<Relative> relatives() {
            return relatives;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Series series = (Series) object;
            return Objects.equals(relatives, series.relatives);
        }

        @Override
        public int hashCode() {
            return Objects.hash(relatives);
        }

        private static DataResult<List<Relative>> validate(List<Relative> relatives) {
            ImmutableList.Builder<Relative> relativesBuilder = ImmutableList.builder();
            boolean temperature = false;
            boolean humidity = false;
            boolean erosion = false;
            boolean weirdness = false;
            for (var click : relatives) {
                if (click.temperature() != Position.CENTER) {
                    if (temperature) {
                        return DataResult.error(() -> "Temperature was claimed twice in this relative series");
                    }
                    temperature = true;
                }
                if (click.humidity() != Position.CENTER) {
                    if (humidity) {
                        return DataResult.error(() -> "Humidity was claimed twice in this relative series");
                    }
                    humidity = true;
                }
                if (click.erosion() != Position.CENTER) {
                    if (erosion) {
                        return DataResult.error(() -> "Erosion was claimed twice in this relative series");
                    }
                    erosion = true;
                }
                if (click.weirdness() != Position.CENTER) {
                    if (weirdness) {
                        return DataResult.error(() -> "Weirdness was claimed twice in this relative series");
                    }
                    weirdness = true;
                }
                relativesBuilder.add(click);
            }
            if (!temperature) {
                return DataResult.error(() -> "Temperature was not claimed in this relative series");
            }
            if (!humidity) {
                return DataResult.error(() -> "Humidity was not claimed in this relative series");
            }
            if (!erosion) {
                return DataResult.error(() -> "Erosion was not claimed in this relative series");
            }
            if (!weirdness) {
                return DataResult.error(() -> "Weirdness was not claimed in this relative series");
            }
            return DataResult.success(relativesBuilder.build());
        }
    }

    public static final Relative.Series DEFAULT = new Relative.Series(List.of(
        new Relative(
            Position.START,
            Position.CENTER,
            Position.CENTER,
            Position.CENTER
        ),
        new Relative(
            Position.CENTER,
            Position.START,
            Position.CENTER,
            Position.CENTER
        ),
        new Relative(
            Position.CENTER,
            Position.CENTER,
            Position.START,
            Position.CENTER
        ),
        new Relative(
            Position.CENTER,
            Position.CENTER,
            Position.CENTER,
            Position.START
        )
    ));
}
