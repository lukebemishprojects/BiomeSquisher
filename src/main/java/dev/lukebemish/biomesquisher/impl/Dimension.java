package dev.lukebemish.biomesquisher.impl;

import com.mojang.serialization.Codec;
import dev.lukebemish.biomesquisher.DimensionBehaviour;
import dev.lukebemish.biomesquisher.Injection;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public enum Dimension implements StringRepresentable {
    TEMPERATURE(0, Climate.ParameterPoint::temperature, Climate.TargetPoint::temperature, NoiseRouter::temperature, true),
    HUMIDITY(1, Climate.ParameterPoint::humidity, Climate.TargetPoint::humidity, NoiseRouter::vegetation, true),
    CONTINENTALNESS(2, Climate.ParameterPoint::continentalness, Climate.TargetPoint::continentalness, NoiseRouter::continents, false),
    EROSION(3, Climate.ParameterPoint::erosion, Climate.TargetPoint::erosion, NoiseRouter::erosion, false),
    DEPTH(4, Climate.ParameterPoint::depth, Climate.TargetPoint::depth, NoiseRouter::depth, false),
    WEIRDNESS(5, Climate.ParameterPoint::weirdness, Climate.TargetPoint::weirdness, NoiseRouter::ridges, false);

    public static final Codec<Dimension> CODEC = StringRepresentable.fromEnum(Dimension::values);
    public static final Dimension[] SQUISH;
    public static final Dimension[] RANGE;
    public static final int[] SQUISH_INDEXES;
    public static final int[] RANGE_INDEXES;
    private final int index;
    private final Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint;
    private final ToLongFunction<Climate.TargetPoint> fromTargetPoint;
    private final Function<NoiseRouter, DensityFunction> fromNoiseRouter;
    private final boolean squish;

    Dimension(int index, Function<Climate.ParameterPoint, Climate.Parameter> fromParameterPoint, ToLongFunction<Climate.TargetPoint> fromTargetPoint, Function<NoiseRouter, DensityFunction> fromNoiseRouter, boolean squish) {
        this.index = index;
        this.fromParameterPoint = fromParameterPoint;
        this.fromTargetPoint = fromTargetPoint;
        this.fromNoiseRouter = fromNoiseRouter;
        this.squish = squish;
    }

    static {
        SQUISH = Arrays.stream(values()).filter(Dimension::squish).toArray(Dimension[]::new);
        RANGE = Arrays.stream(values()).filter(dimension -> dimension != TEMPERATURE && dimension != HUMIDITY).toArray(Dimension[]::new);
        SQUISH_INDEXES = Arrays.stream(SQUISH).mapToInt(Dimension::index).toArray();
        RANGE_INDEXES = Arrays.stream(RANGE).mapToInt(Dimension::index).toArray();
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
        return injection.behaviours()[index];
    }

    public DensityFunction fromNoiseRouter(NoiseRouter router) {
        return fromNoiseRouter.apply(router);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean squish() {
        return squish;
    }
}
