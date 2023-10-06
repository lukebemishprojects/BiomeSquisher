package dev.lukebemish.biomesquisher;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public final class Relative {
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

    public enum Position {
        START(-1),
        CENTER(0),
        END(1);

        private final float offset;

        Position(float offset) {
            this.offset = offset;
        }

        public float offset() {
            return offset;
        }
    }

    public static final class Series {
        private final List<Relative> relatives;

        public Series(List<Relative> relatives) {
            ImmutableList.Builder<Relative> relativesBuilder = ImmutableList.builder();
            boolean temperature = false;
            boolean humidity = false;
            boolean erosion = false;
            boolean weirdness = false;
            for (var click : relatives) {
                if (click.temperature() != Position.CENTER) {
                    if (temperature) {
                        throw new IllegalArgumentException("Temperature was already claimed in this relative series");
                    }
                    temperature = true;
                }
                if (click.humidity() != Position.CENTER) {
                    if (humidity) {
                        throw new IllegalArgumentException("Humidity was already claimed in this relative series");
                    }
                    humidity = true;
                }
                if (click.erosion() != Position.CENTER) {
                    if (erosion) {
                        throw new IllegalArgumentException("Erosion was already claimed in this relative series");
                    }
                    erosion = true;
                }
                if (click.weirdness() != Position.CENTER) {
                    if (weirdness) {
                        throw new IllegalArgumentException("Weirdness was already claimed in this relative series");
                    }
                    weirdness = true;
                }
                relativesBuilder.add(click);
            }
            if (!temperature) {
                throw new IllegalArgumentException("Temperature was not claimed in this relative series");
            }
            if (!humidity) {
                throw new IllegalArgumentException("Humidity was not claimed in this relative series");
            }
            if (!erosion) {
                throw new IllegalArgumentException("Erosion was not claimed in this relative series");
            }
            if (!weirdness) {
                throw new IllegalArgumentException("Weirdness was not claimed in this relative series");
            }
            this.relatives = relativesBuilder.build();
        }

        public List<Relative> relatives() {
            return relatives;
        }
    }
}
