package dev.lukebemish.biomesquisher.test.fabric;

import com.google.auto.service.AutoService;
import dev.lukebemish.biomesquisher.test.TestPlatform;
import dev.lukebemish.biomesquisher.test.TestUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;

@AutoService(TestPlatform.class)
public class TestPlatformImpl implements TestPlatform {
    @Override
    public Collection<Function<String, Path>> testModRoot() {
        return FabricLoader.getInstance().getModContainer(TestUtils.MOD_ID).orElseThrow().getRootPaths().stream().<Function<String, Path>>map(p -> p::resolve).toList();
    }
}
