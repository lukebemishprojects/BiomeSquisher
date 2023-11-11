package dev.lukebemish.biomesquisher.impl;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.lukebemish.biomesquisher.*;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.LevelStem;
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

    private final double[] relativeSizes = new double[] { 1, 1, 1, 1, 1, 1 };

    private final List<Pair<Injection, Holder<Biome>>> injections = new ArrayList<>();

    private final Context context;

    public Squishers(Climate.ParameterList<?> parameterList) {
        this.context = Context.of(parameterList);
    }

    public static void load(ResourceKey<LevelStem> level, Squishers squishers, RegistryAccess registryAccess) {
        List<Pair<ResourceLocation, Series>> loaded = new ArrayList<>();
        for (var entry : registryAccess.registry(BiomeSquisherRegistries.SERIES).orElseThrow(() -> new IllegalStateException("Missing series registry!")).entrySet()) {
            if (entry.getValue().levels().contains(level)) {
                loaded.add(Pair.of(entry.getKey().location(), entry.getValue()));
            }
        }
        loaded.sort(Comparator.comparing(p -> p.getFirst().toString()));
        var squishersRegistry = registryAccess.registry(BiomeSquisherRegistries.SQUISHER).orElseThrow(() -> new IllegalStateException("Missing squisher registry!"));
        for (var pair : loaded) {
            for (var squisherLocation : pair.getSecond().squishers()) {
                var squisher = squishersRegistry.get(squisherLocation);
                if (squisher != null) {
                    squishers.add(squisher);
                } else {
                    Utils.LOGGER.error("Referenced biome squisher {} does not exist!", squisherLocation);
                }
            }
        }
    }

    public void add(Squisher squisher) {
        add(squisher.injection(), squisher.biome(), squisher.relative(), squisher.snap());
    }

    /**
     * "Snap" the provided injection to the nearest corner/edge of biomes, if one exists within the radius.
     * Prefers corners/edges with more dimensions.
     */
    private Injection snap(Injection injection) {
        List<Pair<long[], Double>> candidates = new ArrayList<>();
        int dimensionCount = 0;
        int[] dimensionIdxs = new int[Dimension.values().length];
        long[] initial = new long[Dimension.values().length];
        Dimension[] dimensions = new Dimension[Dimension.values().length];
        for (int i = 0; i < dimensions.length; i++) {
            if (injection.behaviours()[i].isSquish()) {
                dimensionIdxs[i] = dimensionCount;
                initial[dimensionCount] = Utils.decontextQuantizeCoord(injection.behaviours()[i].asSquish().globalPosition());
                dimensions[dimensionCount] = Dimension.values()[i];
                dimensionCount++;
            } else {
                dimensionIdxs[i] = -1;
            }
        }
        outer:
        for (var pair : context.parameterList().values()) {
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
                findCorners(initial, dimensions, i, dimensionCount, candidates::add, current, Utils.quantizeCoord(injection.radius(), context, dimensions[i]), climatePoint);
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
                behaviours[i] = new DimensionBehaviour.Squish(Utils.decontext(
                    Utils.unquantizeAndClamp(center[i], context, dimensions[i]), context, dimensions[i]
                ), dimensions[i].fromInjection(injection).asSquish().degree());
            }
        }
        DimensionBehaviour[] out = new DimensionBehaviour[Dimension.values().length];

        for (int i = 0; i < out.length; i++) {
            if (dimensionIdxs[i] != -1) {
                var behaviour = behaviours[dimensionIdxs[i]];
                if (behaviour != null) {
                    out[i] = behaviour;
                    continue;
                }
            }
            out[i] = injection.behaviours()[i];
        }

        return Injection.of(
            out[0],
            out[1],
            out[2],
            out[3],
            out[4],
            out[5],
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

    private void add(Injection injection, Holder<Biome> biomeHolder, Relative relatives, boolean snap) {
        if (snap) {
            injection = snap(injection);
        }
        injection = injection.remap(p -> reverse(p, relatives), context);
        int squishCount = Dimension.SQUISH_INDEXES.length;
        for (int i : Dimension.SQUISH_INDEXES) {
            relativeSizes[i] = Math.pow(Math.pow(relativeSizes[i], squishCount) + Math.pow(injection.radius(), squishCount), 1.0 / squishCount);
        }
        double relativeVolume = 1;
        for (double relativeSize : relativeSizes) {
            relativeVolume *= relativeSize;
        }
        injections.add(0, Pair.of(injection.scale(relativeVolume), biomeHolder));
    }

    public double[] reverse(double[] target, Relative relatives) {
        for (int i = injections.size() - 1; i >= 0; i--) {
            target = injections.get(i).getFirst().unsquish(target, relatives, context);
        }
        return target;
    }

    public Either<Climate.TargetPoint, Holder<Biome>> apply(Climate.TargetPoint target) {
        for (var pair : injections) {
            target = pair.getFirst().squish(target, context);
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
        for (double relativeSize : relativeSizes) {
            if (relativeSize != 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Squishers{" +
            "injections=" + injections +
            '}';
    }

    public NoiseRouter wrap(NoiseRouter router) {
        DensityFunction[] functions = new DensityFunction[6];
        for (int i = 0; i < 6; i++) {
            if (relativeSizes[i] != 1) {
                functions[i] = wrapHolderHolder(scaledOrElse(unwrapHolderHolder(Dimension.values()[i].fromNoiseRouter(router)), Math.sqrt(relativeSizes[i])));
            } else {
                functions[i] = Dimension.values()[i].fromNoiseRouter(router);
            }
        }
        return new NoiseRouter(
            router.barrierNoise(),
            router.fluidLevelFloodednessNoise(),
            router.fluidLevelSpreadNoise(),
            router.lavaNoise(),
            functions[0],
            functions[1],
            functions[2],
            functions[3],
            functions[4],
            functions[5],
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
