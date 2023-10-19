package dev.lukebemish.biomesquisher.injected;

import dev.lukebemish.biomesquisher.Squishers;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;

public interface Squishable {
    void biomesquisher_squish(Holder<LevelStem> holder, RegistryAccess access, ResourceManager resourceManager);
    @Nullable Squishers biomesquisher_squishers();
}
