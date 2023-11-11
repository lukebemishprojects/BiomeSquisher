package dev.lukebemish.biomesquisher.test;

import dev.lukebemish.biomesquisher.impl.Services;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;

public interface TestPlatform {
    TestPlatform INSTANCE = Services.loadService(TestPlatform.class);
    Collection<Function<String, Path>> testModRoot();
}
