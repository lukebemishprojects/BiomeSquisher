package dev.lukebemish.biomesquisher;

import net.minecraft.world.level.biome.Climate;

import java.util.function.Function;
import java.util.function.ToLongFunction;

public enum Dimension {
    TEMPERATURE(Climate.ParameterPoint::temperature, Climate.TargetPoint::temperature, Injection::temperature),
    HUMIDITY(Climate.ParameterPoint::humidity, Climate.TargetPoint::humidity, Injection::humidity),
    CONTINENTALNESS(Climate.ParameterPoint::continentalness, Climate.TargetPoint::continentalness, Injection::continentalness),
    EROSION(Climate.ParameterPoint::erosion, Climate.TargetPoint::erosion, Injection::erosion),
    DEPTH(Climate.ParameterPoint::depth, Climate.TargetPoint::depth, Injection::depth),
    WEIRDNESS(Climate.ParameterPoint::weirdness, Climate.TargetPoint::weirdness, Injection::weirdness);
    private final Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint;
    private final ToLongFunction<Climate.TargetPoint> fromTargetPoint;
    private final Function<Injection, DimensionBehaviour> fromInjection;

    Dimension(Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint, ToLongFunction<Climate.TargetPoint> fromTargetPoint, Function<Injection, DimensionBehaviour> fromInjection) {
        this.fromParameterPoint = fromParameterPoint;
        this.fromTargetPoint = fromTargetPoint;
        this.fromInjection = fromInjection;
    }

    public Climate.Parameter fromParameterPoint(Climate.ParameterPoint point) {
        return fromParameterPoint.apply(point);
    }

    public long fromTargetPoint(Climate.TargetPoint point) {
        return fromTargetPoint.applyAsLong(point);
    }
    public DimensionBehaviour fromInjection(Injection injection) {
        return fromInjection.apply(injection);
    }
}
