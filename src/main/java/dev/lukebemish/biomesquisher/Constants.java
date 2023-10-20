package dev.lukebemish.biomesquisher;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
    private Constants() {}

    public static final String MOD_ID = "biomesquisher";
    private static final ResourceLocation ROOT = new ResourceLocation(MOD_ID, MOD_ID);
    public static final Logger LOGGER = LoggerFactory.getLogger("Biome Squisher");
    public static final ResourceKey<Registry<Squisher.Series>> SERIES_KEY = ResourceKey.createRegistryKey(id("series"));
    public static final ResourceKey<Registry<Squisher>> SQUISHER_KEY = ResourceKey.createRegistryKey(id("squisher"));

    public static ResourceLocation id(String path) {
        return ROOT.withPath(path);
    }
}
