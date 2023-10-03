package dev.lukebemish.biomesquisher;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.List;

public class Squishers {
    private float cummulativeVolume = 1;
    private static final float MAX_VOLUME = (float) Math.pow(2, 6);
    private final List<Pair<TargetTransformer, Holder<Biome>>> transformers = new ArrayList<>();

    public void add(Pair<TargetTransformer, Holder<Biome>> pair) {
        var transformer = pair.getFirst();
        float newVolume = cummulativeVolume + transformer.volume() / MAX_VOLUME;
        transformers.add(Pair.of(transformer.scale(cummulativeVolume), pair.getSecond()));
        cummulativeVolume = newVolume;
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
}
