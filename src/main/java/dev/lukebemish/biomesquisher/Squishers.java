package dev.lukebemish.biomesquisher;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Squishers {
    private float relativeVolume = 1;
    private final List<Pair<Injection, Holder<Biome>>> injections = new ArrayList<>();

    public void add(Injection injection, Holder<Biome> biomeHolder, Relative.Series relatives) {
        if (!injections.isEmpty()) {
            injection = injection.remap(p -> reverse(p, relatives));
        }
        relativeVolume += injection.relativeVolume();
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

    @Override
    public String toString() {
        return "Squishers{" +
            "injections=" + injections +
            '}';
    }
}
