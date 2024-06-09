package dev.lukebemish.biomesquisher.impl;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.nio.file.Path;
import java.util.Optional;

public interface Platform {
    Platform INSTANCE = Services.loadService(Platform.class);

    Path gameDir();
    Optional<Path> getRootResource(String resource);

    boolean isClient();

    <T> Registry<T> registry(ResourceKey<Registry<T>> key);
}
