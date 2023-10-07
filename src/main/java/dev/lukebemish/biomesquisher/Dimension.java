package dev.lukebemish.biomesquisher;

import net.minecraft.world.level.biome.Climate;

import java.util.function.Function;
import java.util.function.ToLongFunction;

public enum Dimension {
    TEMPERATURE(Climate.ParameterPoint::temperature, Climate.TargetPoint::temperature),
    HUMIDITY(Climate.ParameterPoint::humidity, Climate.TargetPoint::humidity),
    CONTINENTALNESS(Climate.ParameterPoint::continentalness, Climate.TargetPoint::continentalness),
    EROSION(Climate.ParameterPoint::erosion, Climate.TargetPoint::erosion),
    DEPTH(Climate.ParameterPoint::depth, Climate.TargetPoint::depth),
    WEIRDNESS(Climate.ParameterPoint::weirdness, Climate.TargetPoint::weirdness);
    private final Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint;
    private final ToLongFunction<Climate.TargetPoint> fromTargetPoint;

    Dimension(Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint, ToLongFunction<Climate.TargetPoint> fromTargetPoint) {
        this.fromParameterPoint = fromParameterPoint;
        this.fromTargetPoint = fromTargetPoint;
    }

    public Climate.Parameter fromParameterPoint(Climate.ParameterPoint point) {
        return fromParameterPoint.apply(point);
    }

    public long fromTargetPoint(Climate.TargetPoint point) {
        return fromTargetPoint.applyAsLong(point);
    }
}
