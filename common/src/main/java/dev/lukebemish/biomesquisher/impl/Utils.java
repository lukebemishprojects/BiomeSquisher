package dev.lukebemish.biomesquisher.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

    public static long quantizeCoord(double v, Context context, Dimension dimension) {
        return (long) (v * context.quantization().get(dimension));
    }

    public static long decontextQuantizeCoord(double v) {
        return (long) (v * 10000.0d);
    }

    public static double unquantizeAndClamp(long coord, Context context, Dimension dimension) {
        return Mth.clamp(coord / context.quantization().get(dimension), -1, 1);
    }

    public static double decontext(double v, Context context, Dimension dimension) {
        return v * context.quantization().get(dimension) / 10000.0d;
    }

    public static double recontext(double v, Context context, Dimension dimension) {
        return v * 10000.0d / context.quantization().get(dimension);
    }
}
