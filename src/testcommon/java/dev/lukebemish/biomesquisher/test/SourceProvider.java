package dev.lukebemish.biomesquisher.test;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.function.Function;

public interface SourceProvider {
    <T> Climate.ParameterList<T> biomesquisher_test_apply(Function<ResourceKey<Biome>, T> function);
}
