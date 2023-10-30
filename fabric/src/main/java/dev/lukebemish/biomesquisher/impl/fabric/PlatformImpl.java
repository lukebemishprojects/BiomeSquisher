package dev.lukebemish.biomesquisher.impl.fabric;

import com.google.auto.service.AutoService;
import dev.lukebemish.biomesquisher.impl.Platform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {
    @Override
    public Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }
}
