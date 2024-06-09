package dev.lukebemish.biomesquisher.impl.fabric;

import com.google.auto.service.AutoService;
import dev.lukebemish.biomesquisher.impl.Platform;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.nio.file.Path;
import java.util.Optional;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {
    @Override
    public Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Optional<Path> getRootResource(String resource) {
        return FabricLoader.getInstance().getModContainer(Utils.MOD_ID).orElseThrow().findPath(resource);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public <T> Registry<T> registry(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).buildAndRegister();
    }
}
