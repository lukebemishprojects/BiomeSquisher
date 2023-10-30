package dev.lukebemish.biomesquisher.impl;

import java.util.ServiceLoader;

public class Services {
    public static <T> T loadService(Class<T> clazz) {
        return ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
    }
}
