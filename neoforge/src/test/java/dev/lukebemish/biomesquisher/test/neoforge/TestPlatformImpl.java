package dev.lukebemish.biomesquisher.test.neoforge;

import com.google.auto.service.AutoService;
import dev.lukebemish.biomesquisher.test.TestPlatform;
import dev.lukebemish.biomesquisher.test.TestUtils;
import net.neoforged.fml.ModList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@AutoService(TestPlatform.class)
public class TestPlatformImpl implements TestPlatform {
    @Override
    public Collection<Function<String, Path>> testModRoot() {
        return List.of(ModList.get().getModContainerById(TestUtils.MOD_ID).orElseThrow().getModInfo().getOwningFile().getFile()::findResource);
    }
}
