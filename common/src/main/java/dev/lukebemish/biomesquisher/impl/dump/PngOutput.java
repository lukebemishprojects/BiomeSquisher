package dev.lukebemish.biomesquisher.impl.dump;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import dev.lukebemish.biomesquisher.impl.Platform;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PngOutput implements BiomeDumper.Output {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.ROOT);
    public static final PngOutput INSTANCE_1024 = new PngOutput(1024);
    public static final PngOutput INSTANCE_256 = new PngOutput(256);

    private final int resolution;
    private PngOutput(int resolution) {
        this.resolution = resolution;
    }

    public <T> Map<ResourceKey<Biome>, Integer> dumpImage(BiFunction<Float, Float, Holder<Biome>> biomeGetter, Set<Holder<Biome>> possibleBiomes, Function<Integer, T> makeRow, BiConsumer<Integer, T> writeRow, TriConsumer<T, Integer, Integer> writeValue) {
        Map<ResourceKey<Biome>, Integer> hash = biomeColorHash(possibleBiomes);
        for (int i = 0; i < resolution; i++) {
            T line = makeRow.apply(i);
            for (int j = 0; j < resolution; j++) {
                int finalJ = j;
                biomeGetter.apply(i/((float)resolution), j/((float)resolution))
                    .unwrapKey().ifPresent(key -> {
                        int color = hash.getOrDefault(key, 0);
                        writeValue.accept(line, finalJ, color);
                    });
            }
            writeRow.accept(i, line);
        }
        return hash;
    }

    @NotNull
    public static Map<ResourceKey<Biome>, Integer> biomeColorHash(Set<Holder<Biome>> possibleBiomes) {
        return possibleBiomes.stream().map(h -> h.unwrapKey().orElse(null)).filter(Objects::nonNull)
            .collect(Collectors.toMap(Function.identity(), BiomeDumper::hashBiome));
    }

    public Map<ResourceKey<Biome>, Integer> dumpToOutput(OutputStream os, BiFunction<Float, Float, Holder<Biome>> biomeGetter, Set<Holder<Biome>> possibleBiomes) {
        Map<ResourceKey<Biome>, Integer> hash;
        PngWriter writer = new PngWriter(os, new ImageInfo(
            resolution, resolution, 8, true
        ));
        hash = dumpImage(
            biomeGetter,
            possibleBiomes,
            i -> new ImageLineInt(writer.imgInfo),
            (i, line) -> writer.writeRow(line),
            (line, j, color) -> ImageLineHelper.setPixelRGBA8(line, j, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF)
        );
        writer.end();
        return hash;
    }

    @Override
    public void dump(Level level, BiFunction<Float, Float, Holder<Biome>> biomeGetter, Set<Holder<Biome>> possibleBiomes) throws IOException {
        var now = new Date();
        Map<ResourceKey<Biome>, Integer> hash;
        Path outputDir = Platform.INSTANCE.gameDir().resolve("dumps").resolve("biomesquisher");
        String levelId = level.dimensionTypeId().location().getNamespace() + "." + level.dimensionTypeId().location().getPath();
        Path output = outputDir.resolve(levelId + "." + DATE_FORMAT.format(now) + ".png");
        Path outputKey = outputDir.resolve(levelId + ".key.txt");
        Path outputKeyHtml = outputDir.resolve(levelId + ".key.html");
        Files.createDirectories(outputDir);
        try (OutputStream os = Files.newOutputStream(output)) {
            hash = dumpToOutput(os, biomeGetter, possibleBiomes);
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
    }
}
