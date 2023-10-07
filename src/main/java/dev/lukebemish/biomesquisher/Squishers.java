package dev.lukebemish.biomesquisher;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Squishers {
    private float relativeSizeTemperature = 1;
    private float relativeSizeHumidity = 1;
    private float relativeSizeErosion = 1;
    private float relativeSizeWeirdness = 1;

    private final List<Pair<Injection, Holder<Biome>>> injections = new ArrayList<>();

    public void add(Injection injection, Holder<Biome> biomeHolder, Relative.Series relatives) {
        injection = injection.remap(p -> reverse(p, relatives));
        boolean isTemperature = injection.temperature().asSquish() != null;
        boolean isHumidity = injection.humidity().asSquish() != null;
        boolean isErosion = injection.erosion().asSquish() != null;
        boolean isWeirdness = injection.weirdness().asSquish() != null;
        int dimensions = (isTemperature ? 1 : 0) + (isHumidity ? 1 : 0) + (isErosion ? 1 : 0) + (isWeirdness ? 1 : 0);
        if (isTemperature) {
            relativeSizeTemperature = (float) Math.pow(Math.pow(relativeSizeTemperature, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        if (isHumidity) {
            relativeSizeHumidity = (float) Math.pow(Math.pow(relativeSizeHumidity, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        if (isErosion) {
            relativeSizeErosion = (float) Math.pow(Math.pow(relativeSizeErosion, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        if (isWeirdness) {
            relativeSizeWeirdness = (float) Math.pow(Math.pow(relativeSizeWeirdness, dimensions) + Math.pow(injection.radius(), dimensions), 1.0 / dimensions);
        }
        float relativeVolume = (isTemperature ? relativeSizeTemperature : 1) * (isHumidity ? relativeSizeHumidity : 1) * (isErosion ? relativeSizeErosion : 1) * (isWeirdness ? relativeSizeWeirdness : 1);
        injections.add(0, Pair.of(injection.scale(relativeVolume), biomeHolder));
    }

    public Climate.TargetPoint reverse(Climate.TargetPoint target, Relative.Series relatives) {
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
        var temperature = relativeSizeTemperature == 1 ? router.temperature() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.temperature()), Mth.sqrt(relativeSizeTemperature)));
        var humidity = relativeSizeHumidity == 1 ? router.vegetation() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.vegetation()), Mth.sqrt(relativeSizeHumidity)));
        var erosion = relativeSizeErosion == 1 ? router.erosion() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.erosion()), Mth.sqrt(relativeSizeErosion)));
        var weirdness = relativeSizeWeirdness == 1 ? router.ridges() : wrapHolderHolder(scaledOrElse(unwrapHolderHolder(router.ridges()), Mth.sqrt(relativeSizeWeirdness)));
        System.out.println("Temperature: " + temperature);
        System.out.println("Humidity: " + humidity);
        System.out.println("Erosion: " + erosion);
        System.out.println("Weirdness: " + weirdness);
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

    private static DensityFunction scaledOrElse(DensityFunction input, float scale) {
        var scaler = new InternalScalingSampler.SetScale(scale);
        var scaled = scaler.apply(input);
        if (scaler.scaled()) {
            return scaled;
        }
        return new InternalScalingSampler(input, scale);
    }
}
