package dev.lukebemish.biomesquisher.injected;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

public interface Squishable {
    void biomesquisher_squish(ResourceKey<LevelStem> key, RegistryAccess access);
}
