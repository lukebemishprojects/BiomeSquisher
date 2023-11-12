package dev.lukebemish.biomesquisher.impl.neoforge;

import com.google.auto.service.AutoService;
import dev.lukebemish.biomesquisher.impl.Platform;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {
    @Override
    public Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Optional<Path> getRootResource(String resource) {
        Path path = ModList.get().getModFileById(Utils.MOD_ID).getFile().findResource(resource);
        if (Files.exists(path)) {
            return Optional.of(path);
        }
        return Optional.empty();
    }
}
