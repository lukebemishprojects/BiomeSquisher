package dev.lukebemish.biomesquisher.impl.injected;

import dev.lukebemish.biomesquisher.impl.Squishers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;

public interface Squishable {
    void biomesquisher_squish(ResourceKey<LevelStem> holder, RegistryAccess access, ResourceManager resourceManager);
    @Nullable Squishers biomesquisher_squishers();
}
