package dev.lukebemish.biomesquisher;

import java.util.Objects;

public final class Click {
    private final ClickPosition temperature;
    private final ClickPosition humidity;
    private final ClickPosition continentalness;
    private final ClickPosition erosion;
    private final ClickPosition weirdness;

    public Click(ClickPosition temperature, ClickPosition humidity, ClickPosition continentalness, ClickPosition erosion, ClickPosition weirdness) {
        if (temperature == ClickPosition.CENTER && humidity == ClickPosition.CENTER && continentalness == ClickPosition.CENTER && erosion == ClickPosition.CENTER && weirdness == ClickPosition.CENTER) {
            throw new IllegalArgumentException("All click positions cannot be CENTER");
        }
        this.temperature = temperature;
        this.humidity = humidity;
        this.continentalness = continentalness;
        this.erosion = erosion;
        this.weirdness = weirdness;
    }

    public ClickPosition temperature() {
        return temperature;
    }

    public ClickPosition humidity() {
        return humidity;
    }

    public ClickPosition continentalness() {
        return continentalness;
    }

    public ClickPosition erosion() {
        return erosion;
    }

    public ClickPosition weirdness() {
        return weirdness;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Click click) {
            return Objects.equals(this.temperature, click.temperature) &&
                Objects.equals(this.humidity, click.humidity) &&
                Objects.equals(this.continentalness, click.continentalness) &&
                Objects.equals(this.erosion, click.erosion) &&
                Objects.equals(this.weirdness, click.weirdness);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, humidity, continentalness, erosion, weirdness);
    }

    @Override
    public String toString() {
        return "Click{" +
                "temperature=" + temperature +
                ", humidity=" + humidity +
                ", continentalness=" + continentalness +
                ", erosion=" + erosion +
                ", weirdness=" + weirdness +
            '}';
    }

    public enum ClickPosition {
        START,
        CENTER,
        END
    }
}
