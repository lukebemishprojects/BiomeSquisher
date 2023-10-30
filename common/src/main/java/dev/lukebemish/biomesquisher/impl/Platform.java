package dev.lukebemish.biomesquisher.impl;

import java.nio.file.Path;

public interface Platform {
    Platform INSTANCE = Services.loadService(Platform.class);

    Path gameDir();
}
