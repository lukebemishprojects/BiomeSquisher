package dev.lukebemish.biomesquisher.impl;

import java.nio.file.Path;
import java.util.Optional;

public interface Platform {
    Platform INSTANCE = Services.loadService(Platform.class);

    Path gameDir();
    Optional<Path> getRootResource(String resource);
}
