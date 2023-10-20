package dev.lukebemish.biomesquisher.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    private Utils() {}

    public static final String MOD_ID = "biomesquisher";
    private static final ResourceLocation ROOT = new ResourceLocation(MOD_ID, MOD_ID);
    public static final Logger LOGGER = LoggerFactory.getLogger("Biome Squisher");

    public static ResourceLocation id(String path) {
        return ROOT.withPath(path);
    }

    public static long quantizeCoord(double v) {
        return (long) (v * 10000.0);
    }

    public static double unquantizeAndClamp(long coord) {
        return Mth.clamp(Climate.unquantizeCoord(coord), -1, 1);
    }
}
