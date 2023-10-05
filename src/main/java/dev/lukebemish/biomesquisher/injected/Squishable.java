package dev.lukebemish.biomesquisher.injected;

import dev.lukebemish.biomesquisher.Squishers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;

public interface Squishable {
    void biomesquisher_squish(ResourceKey<LevelStem> key, RegistryAccess access);
    @Nullable Squishers biomesquisher_squishers();
}
