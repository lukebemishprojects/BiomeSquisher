package dev.lukebemish.biomesquisher.impl;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.lukebemish.biomesquisher.*;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Squishers {
    private double relativeSizeTemperature = 1;
    private double relativeSizeHumidity = 1;
    private double relativeSizeErosion = 1;
    private double relativeSizeWeirdness = 1;

    private final List<Pair<Injection, Holder<Biome>>> injections = new ArrayList<>();

    private final Climate.ParameterList<?> parameterList;

    public Squishers(Climate.ParameterList<?> parameterList) {
        this.parameterList = parameterList;
    }

    private Injection snap(Injection injection) {
        List<Pair<long[], Double>> candidates = new ArrayList<>();
        int dimensionCount = 0;
        int temperature = -1;
        int humidity = -1;
        int erosion = -1;
        int weirdness = -1;
        long[] initial = new long[4];
        Dimension[] dimensions = new Dimension[4];
        if (injection.temperature().asSquish() != null) {
            temperature = dimensionCount;
            dimensionCount++;
            initial[temperature] = Utils.quantizeCoord(injection.temperature().asSquish().center());
            dimensions[temperature] = Dimension.TEMPERATURE;
        }
        if (injection.humidity().asSquish() != null) {
            humidity = dimensionCount;
            dimensionCount++;
            initial[humidity] = Utils.quantizeCoord(injection.humidity().asSquish().center());
            dimensions[humidity] = Dimension.HUMIDITY;
        }
        if (injection.erosion().asSquish() != null) {
            erosion = dimensionCount;
            dimensionCount++;
            initial[erosion] = Utils.quantizeCoord(injection.erosion().asSquish().center());
            dimensions[erosion] = Dimension.EROSION;
        }
        if (injection.weirdness().asSquish() != null) {
            weirdness = dimensionCount;
            dimensionCount++;
            initial[weirdness] = Utils.quantizeCoord(injection.weirdness().asSquish().center());
            dimensions[weirdness] = Dimension.WEIRDNESS;
        }
        outer:
        for (var pair : parameterList.values()) {
            var climatePoint = pair.getFirst();
            for (int i = 0; i < dimensionCount; i++) {
                if (
                    Math.abs(initial[i] - dimensions[i].fromParameterPoint(climatePoint).min()) <= injection.radius() ||
                    Math.abs(initial[i] - dimensions[i].fromParameterPoint(climatePoint).max()) <= injection.radius() ||
                    (initial[i] <= dimensions[i].fromParameterPoint(climatePoint).max() && initial[i] >= dimensions[i].fromParameterPoint(climatePoint).min())
                ) {
                    continue;
                }
                continue outer;
            }
            long[] current = new long[] {
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                Long.MAX_VALUE
            };
            for (int i = 0; i < dimensionCount; i++) {
                findCorners(initial, dimensions, i, dimensionCount, candidates::add, current, Utils.quantizeCoord(injection.radius()), climatePoint);
            }
        }
        candidates.sort(CORNER_COMPARATOR);
        if (candidates.isEmpty()) {
            return injection;
        }
        long[] center = candidates.get(0).getFirst();
        DimensionBehaviour[] behaviours = new DimensionBehaviour[dimensionCount];
        for (int i = 0; i < dimensionCount; i++) {
            if (center[i] != Long.MAX_VALUE) {
                //noinspection DataFlowIssue
                behaviours[i] = new DimensionBehaviour.Squish(Utils.unquantizeAndClamp(center[i]), dimensions[i].fromInjection(injection).asSquish().degree());
            }
        }
        DimensionBehaviour temperatureOut = temperature == -1 ? injection.temperature() : (behaviours[temperature] == null ? injection.temperature() : behaviours[temperature]);
        DimensionBehaviour humidityOut = humidity == -1 ? injection.humidity() : (behaviours[humidity] == null ? injection.humidity() : behaviours[humidity]);
        DimensionBehaviour erosionOut = erosion == -1 ? injection.erosion() : (behaviours[erosion] == null ? injection.erosion() : behaviours[erosion]);
        DimensionBehaviour weirdnessOut = weirdness == -1 ? injection.weirdness() : (behaviours[weirdness] == null ? injection.weirdness() : behaviours[weirdness]);

        return Injection.of(
            temperatureOut,
            humidityOut,
            injection.continentalness(),
            erosionOut,
            injection.depth(),
            weirdnessOut,
            injection.radius()
        );
    }

    private static final Comparator<Pair<long[], Double>> CORNER_COMPARATOR = (o1, o2) -> {
        int count1 = 0;
        int count2 = 0;
        for (int i = 0; i < o1.getFirst().length; i++) {
            if (o1.getFirst()[i] != Long.MAX_VALUE) {
                count1++;
            }
            if (o2.getFirst()[i] != Long.MAX_VALUE) {
                count2++;
            }
        }
        if (count1 != count2) {
            return -Integer.compare(count1, count2);
        }
        return Double.compare(o1.getSecond(), o2.getSecond());
    };

    private static void findCorners(long[] initial, Dimension[] dimensions, int currentDimension, int dimensionCount, Consumer<Pair<long[], Double>> consumer, long[] current, long radius, Climate.ParameterPoint parameterPoint) {
        long start = dimensions[currentDimension].fromParameterPoint(parameterPoint).min();
        long end = dimensions[currentDimension].fromParameterPoint(parameterPoint).max();
        long center = initial[currentDimension];
        if (Math.abs(center - start) <= radius) {
            current[currentDimension] = start;
            consumer.accept(Pair.of(Arrays.copyOf(current, dimensionCount), cornerDistance(initial, current)));
            if (currentDimension != dimensionCount - 1) {
                for (int i = currentDimension + 1; i < dimensionCount; i++) {
                    findCorners(initial, dimensions, i, dimensionCount, consumer, current, radius, parameterPoint);
                }
            }
        }
        if (Math.abs(center - end) <= radius) {
            current[currentDimension] = end;
            consumer.accept(Pair.of(Arrays.copyOf(current, dimensionCount), cornerDistance(initial, current)));
            if (currentDimension != dimensionCount - 1) {
                for (int i = currentDimension + 1; i < dimensionCount; i++) {
                    findCorners(initial, dimensions, i, dimensionCount, consumer, current, radius, parameterPoint);
                }
            }
        }
        current[currentDimension] = Long.MAX_VALUE;
    }

    private static double cornerDistance(long[] initial, long[] corner) {
        double distance = 0;
        for (int i = 0; i < initial.length; i++) {
            if (initial[i] == Long.MAX_VALUE || corner[i] == Long.MAX_VALUE) {
                break;
            }
            distance += Math.pow(initial[i] - corner[i], 2);
        }
        return Math.sqrt(distance);
    }

    public void add(Squisher squisher) {
        add(squisher.injection(), squisher.biome(), squisher.relative(), squisher.snap());
    }

    private void add(Injection injection, Holder<Biome> biomeHolder, Relative.Series relatives, boolean snap) {
        if (snap) {
            injection = snap(injection);
        }
        injection = injection.remap(p -> reverse(p, relatives));
        boolean isTemperature = injection.temperature().asSquish() != null;
        boolean isHumidity = injection.humidity().asSquish() != null;
        boolean isErosion = injection.erosion().asSquish() != null;
        boolean isWeirdness = injection.weirdness().asSquish() != null;
        int dimensions = (isTemperature ? 1 : 0) + (isHumidity ? 1 : 0) + (isErosion ? 1 : 0) + (isWeirdness ? 1 : 0);
        if (isTemperature) {
            relativeSizeTemperature = Math.pow(Math.pow(relativeSizeTemperature, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        if (isHumidity) {
            relativeSizeHumidity = Math.pow(Math.pow(relativeSizeHumidity, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        if (isErosion) {
            relativeSizeErosion = Math.pow(Math.pow(relativeSizeErosion, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        if (isWeirdness) {
            relativeSizeWeirdness = Math.pow(Math.pow(relativeSizeWeirdness, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        double relativeVolume = (isTemperature ? relativeSizeTemperature : 1) * (isHumidity ? relativeSizeHumidity : 1) * (isErosion ? relativeSizeErosion : 1) * (isWeirdness ? relativeSizeWeirdness : 1);
        injections.add(0, Pair.of(injection.scale(relativeVolume), biomeHolder));
    }

    public double[] reverse(double[] target, Relative.Series relatives) {
        for (int i = injections.size() - 1; i >= 0; i--) {
            target = injections.get(i).getFirst().unsquish(target, relatives);
        }
        return target;
    }

    public Either<Climate.TargetPoint, Holder<Biome>> apply(Climate.TargetPoint target) {
        for (var pair : injections) {
            target = pair.getFirst().squish(target);
            if (target == null) {
                return Either.right(pair.getSecond());
            }
        }
        return Either.left(target);
    }

    public Stream<Holder<Biome>> possibleBiomes() {
        return injections.stream().map(Pair::getSecond);
    }

    public boolean needsSpacialScaling() {
        return relativeSizeTemperature != 1 || relativeSizeHumidity != 1 || relativeSizeErosion != 1 || relativeSizeWeirdness != 1;
    }

    @Override
    public String toString() {
        return "Squishers{" +
            "injections=" + injections +
            '}';
    }

    public NoiseRouter wrap(NoiseRouter router) {
        var temperature = relativeSizeTemperature == 1 ? router.temperature() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.temperature()), Math.sqrt(relativeSizeTemperature)));
        var humidity = relativeSizeHumidity == 1 ? router.vegetation() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.vegetation()), Math.sqrt(relativeSizeHumidity)));
        var erosion = relativeSizeErosion == 1 ? router.erosion() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.erosion()), Math.sqrt(relativeSizeErosion)));
        var weirdness = relativeSizeWeirdness == 1 ? router.ridges() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.ridges()), Math.sqrt(relativeSizeWeirdness)));
        return new NoiseRouter(
            router.barrierNoise(),
            router.fluidLevelFloodednessNoise(),
            router.fluidLevelSpreadNoise(),
            router.lavaNoise(),
            temperature,
            humidity,
            router.continents(),
            erosion,
            router.depth(),
            weirdness,
            router.initialDensityWithoutJaggedness(),
            router.finalDensity(),
            router.veinToggle(),
            router.veinRidged(),
            router.veinGap()
        );
    }

    private static DensityFunction unwrapHolderHolder(DensityFunction function) {
        if (function instanceof DensityFunctions.HolderHolder holderHolder) {
            return unwrapHolderHolder(holderHolder.function().value());
        }
        return function;
    }

    private static DensityFunction wrapHolderHolder(DensityFunction function) {
        if (function instanceof DensityFunctions.HolderHolder) {
            return function;
        }
        return new DensityFunctions.HolderHolder(Holder.direct(function));
    }

    private static DensityFunction scaledOrElse(DensityFunction input, double scale) {
        var scaler = new InternalScalingSampler.SetScale((float) scale);
        var scaled = scaler.apply(input);
        if (scaler.scaled()) {
            return scaled;
        }
        return new InternalScalingSampler(input, (float) scale);
    }
}
