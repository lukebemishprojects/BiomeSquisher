package dev.lukebemish.biomesquisher.impl.injected;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public interface KnowsOriginalKey {
    ResourceKey<NoiseGeneratorSettings> biomesquisher_generatorKey();
    void biomesquisher_generatorKey(ResourceKey<NoiseGeneratorSettings> key);
}
