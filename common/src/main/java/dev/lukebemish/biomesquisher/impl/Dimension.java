package dev.lukebemish.biomesquisher.impl;

import com.mojang.serialization.Codec;
import dev.lukebemish.biomesquisher.DimensionBehaviour;
import dev.lukebemish.biomesquisher.Injection;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public enum Dimension implements StringRepresentable {
    TEMPERATURE(0, Climate.ParameterPoint::temperature, Climate.TargetPoint::temperature, Injection::temperature),
    HUMIDITY(1, Climate.ParameterPoint::humidity, Climate.TargetPoint::humidity, Injection::humidity),
    CONTINENTALNESS(2, Climate.ParameterPoint::continentalness, Climate.TargetPoint::continentalness, Injection::continentalness),
    EROSION(3, Climate.ParameterPoint::erosion, Climate.TargetPoint::erosion, Injection::erosion),
    DEPTH(4, Climate.ParameterPoint::depth, Climate.TargetPoint::depth, Injection::depth),
    WEIRDNESS(5, Climate.ParameterPoint::weirdness, Climate.TargetPoint::weirdness, Injection::weirdness);
    public static final Codec<Dimension> CODEC = StringRepresentable.fromEnum(Dimension::values);
    private final int index;
    private final Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint;
    private final ToLongFunction<Climate.TargetPoint> fromTargetPoint;
    private final Function<Injection, DimensionBehaviour> fromInjection;

    Dimension(int index, Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint, ToLongFunction<Climate.TargetPoint> fromTargetPoint, Function<Injection, DimensionBehaviour> fromInjection) {
        this.index = index;
        this.fromParameterPoint = fromParameterPoint;
        this.fromTargetPoint = fromTargetPoint;
        this.fromInjection = fromInjection;
    }

    public int index() {
        return index;
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

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
