package dev.lukebemish.biomesquisher;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
    private Constants() {}

    public static final String MOD_ID = "biomesquisher";
    public static final Logger LOGGER = LoggerFactory.getLogger("Biome Squisher");

    private static final ResourceLocation ROOT = new ResourceLocation(MOD_ID, MOD_ID);

    public static ResourceLocation id(String path) {
        return ROOT.withPath(path);
    }
}
