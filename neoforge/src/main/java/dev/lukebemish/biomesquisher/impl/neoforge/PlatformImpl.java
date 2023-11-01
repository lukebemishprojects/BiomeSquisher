package dev.lukebemish.biomesquisher.impl.neoforge;

import com.google.auto.service.AutoService;
import dev.lukebemish.biomesquisher.impl.Platform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {
    @Override
    public Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }
}
