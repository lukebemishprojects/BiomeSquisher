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
    private float cummulativeVolume = 1;
    private static final float MAX_VOLUME = (float) Math.pow(2, 6);
    private final List<Pair<TargetTransformer, Holder<Biome>>> transformers = new ArrayList<>();

    public void add(Pair<TargetTransformer, Holder<Biome>> pair) {
        var transformer = pair.getFirst();
        cummulativeVolume += transformer.volume() / MAX_VOLUME;
        transformers.add(Pair.of(transformer.scale(cummulativeVolume), pair.getSecond()));
    }

    public Either<Climate.TargetPoint, Holder<Biome>> apply(Climate.TargetPoint target) {
        for (var pair : transformers) {
            target = pair.getFirst().squish(target);
            if (target == null) {
                return Either.right(pair.getSecond());
            }
        }
        return Either.left(target);
    }

    public Stream<Holder<Biome>> possibleBiomes() {
        return transformers.stream().map(Pair::getSecond);
    }

    @Override
    public String toString() {
        return "Squishers{" +
            "transformers=" + transformers +
            '}';
    }
}
