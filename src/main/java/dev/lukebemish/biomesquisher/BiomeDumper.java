package dev.lukebemish.biomesquisher;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import dev.lukebemish.biomesquisher.mixin.MultiNoiseBiomeSourceAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BiomeDumper {
    public enum Dimension {
        TEMPERATURE(0),
        HUMIDITY(1),
        CONTINENTALNESS(2),
        EROSION(3),
        DEPTH(4),
        WEIRDNESS(5);

        public static final String[] EXAMPLES = Arrays.stream(values()).map(v -> v.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);
        private final int index;

        Dimension(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.ROOT);

    public record SliceLocation(float i, float j, float k, float l) {
        public long getAt(int index) {
            float val = switch (index) {
                case 0 -> i;
                case 1 -> j;
                case 2 -> k;
                case 3 -> l;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
            return Climate.quantizeCoord(val);
        }
    }

    public static void dump(Level level, MultiNoiseBiomeSource source, Dimension x, Dimension y, SliceLocation location) {
        Climate.ParameterList<Holder<Biome>> parameters = ((MultiNoiseBiomeSourceAccessor) source).biomesquisher_parameters();
        Map<ResourceKey<Biome>, Integer> hash = source.possibleBiomes().stream().map(h -> h.unwrapKey().orElse(null)).filter(Objects::nonNull)
            .collect(Collectors.toMap(Function.identity(), BiomeDumper::hashBiome));
        long[] indices = new long[6];
        int indexed = 0;
        for (int i = 0; i < 6; i++) {
            if (i == x.index() || i == y.index()) {
                continue;
            }
            indices[i] = location.getAt(indexed);
        }
        var now = new Date();
        Path outputDir = FabricLoader.getInstance().getGameDir().resolve("dumps").resolve("biomesquisher");
        String levelId = level.dimensionTypeId().location().getNamespace() + "." + level.dimensionTypeId().location().getPath();
        Path output = outputDir.resolve(levelId + "." + DATE_FORMAT.format(now) + ".png");
        Path outputKey = outputDir.resolve(levelId + ".key.txt");
        Path outputKeyHtml = outputDir.resolve(levelId + ".key.html");
        try {
            Files.createDirectories(outputDir);
            try (OutputStream os = Files.newOutputStream(output)) {
                PngWriter writer = new PngWriter(os, new ImageInfo(
                    1024, 1024, 8, true
                ));
                for (int i = 0; i < 1024; i++) {

                    ImageLineInt line = new ImageLineInt(writer.imgInfo);
                    for (int j = 0; j < 1024; j++) {
                        indices[x.index()] = Climate.quantizeCoord(i/512f - 1);
                        indices[y.index()] = Climate.quantizeCoord(j/512f - 1);
                        int finalJ = j;
                        parameters.findValue(new Climate.TargetPoint(
                            indices[0], indices[1], indices[2], indices[3], indices[4], indices[5]
                        )).unwrapKey().ifPresent(key -> {
                            int color = hash.getOrDefault(key, 0);
                            ImageLineHelper.setPixelRGBA8(line, finalJ, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
                        });
                    }
                    writer.writeRow(line);
                }
                writer.end();
            }
            try (var writer = new OutputStreamWriter(Files.newOutputStream(outputKey))) {
                int maxLength = hash.keySet().stream().mapToInt(k -> k.location().toString().length()).max().orElse(0);
                for (var entry : hash.entrySet()) {
                    String key = entry.getKey().location().toString();
                    writer.write(key);
                    for (int i = 0; i < maxLength - key.length(); i++) {
                        writer.write(" ");
                    }
                    writer.write(" : 0x"+Integer.toHexString(entry.getValue()).substring(2).toUpperCase(Locale.ROOT));
                    writer.write("\n");
                }
            }
            try (var writer = new OutputStreamWriter(Files.newOutputStream(outputKeyHtml))) {
                writer.write("<!DOCTYPE html><html><head><title>Biome Squisher Key</title></head><body><table><thead><tr><th>Biome</th><th>Color</th></th><th>Sample</th></tr></thead><tbody>");
                for (var entry : hash.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().location().toString())).toList()) {
                    String key = entry.getKey().location().toString();
                    String colorString = Integer.toHexString(entry.getValue()).substring(2).toUpperCase(Locale.ROOT);
                    writer.write(
                        "<tr><td>"+key+"</td><td>0x"+colorString+"</td><td style=\"background-color: #"+colorString+"; width: 100%\"></td></tr>"
                    );
                }
                writer.write("</tbody></table></body></html>");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int hashBiome(ResourceKey<Biome> key) {
        return (key.location().hashCode() & 0xFFFFFF) | 0xFF000000;
    }
}
