package dev.lukebemish.biomesquisher.test;

import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.impl.dump.BiomeDumper;
import dev.lukebemish.biomesquisher.impl.dump.PngOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.LevelStem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class BiomeSquisherGameTests {
    private static void testLayout(GameTestHelper context, LayoutTest test) {
        var multiNoiseBiomeSource = getOverworldBiomeList(context.getLevel(), context.getLevel().getServer().getResourceManager());
        int[][] data = new int[1024][];
        try {
            BiomeDumper.dump(context.getLevel(), multiNoiseBiomeSource, test.specs().x(), test.specs().y(), test.specs().slice(), (level, biomeGetter, posibleBiomes) ->
                    PngOutput.INSTANCE.dumpImage(biomeGetter, posibleBiomes, i -> new int[1024], (i, row) -> data[i] = row, (row, col, v) -> row[col] = v),
                test.specs().frame()
            );
        } catch (IOException e) {
            Utils.LOGGER.error("Failed to save biome dump", e);
            context.fail("Failed to save biome dump");
            return;
        }
        context.assertTrue(test.target().equals(new LayoutTest.Layout(data)), "Biome layout "+test.location()+" does not match expected layout.");
        context.succeed();
    }

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    @GameTestGenerator
    public static Collection<TestFunction> testLayoutsGenerator() {
        List<LayoutTest> layouts = new ArrayList<>();
        Collection<Function<String, Path>> roots = TestPlatform.INSTANCE.testModRoot();
        for (var rootGetter : roots) {
            var root = rootGetter.apply("test").resolve("layouts");
            try (var stream = Files.walk(root)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".json") && !Files.isDirectory(path)).forEach(path -> {
                    Path png = path.getParent().resolve(path.getFileName().toString().replace(".json", ".png"));
                    if (!Files.exists(png)) {
                        String message = "Missing png for test layout: " + path;
                        Utils.LOGGER.error(message);
                        throw new RuntimeException(message);
                    }
                    String[] parts = new String[root.relativize(path).getNameCount()];
                    for (int i = 0; i < parts.length; i++) {
                        parts[i] = root.relativize(path).getName(i).toString();
                    }
                    String testName = String.join("/", parts).replace(".json", "");
                    try (var pngStream = Files.newInputStream(png);
                         var jsonReader = Files.newBufferedReader(path)) {
                        PngReader r = new PngReader(pngStream);
                        if (r.imgInfo.cols != 1024 || r.imgInfo.rows != 1024) {
                            var message = "Invalid png size for test layout: " + testName;
                            Utils.LOGGER.error(message);
                            throw new RuntimeException(message);
                        }
                        JsonElement json = GSON.fromJson(jsonReader, JsonElement.class);
                        var result = LayoutTest.LayoutSpecs.CODEC.parse(JsonOps.INSTANCE, json).mapError(e -> {
                            Utils.LOGGER.error("Failed to parse test layout {}: {}", testName, e);
                            return e;
                        });
                        if (result.error().isPresent()) {
                            throw new RuntimeException(result.error().get().message());
                        }
                        LayoutTest.LayoutSpecs specs = result.result().orElseThrow();
                        int[][] data = new int[1024][1024];
                        for (int i = 0; i < 1024; i++) {
                            int[] row = data[i];
                            ImageLineInt rRow = (ImageLineInt) r.readRow(i);
                            for (int j = 0; j < 1024; j++) {
                                row[j] = rRow.getElem(j);
                            }
                        }
                        layouts.add(new LayoutTest(testName, specs, new LayoutTest.Layout(data)));
                    } catch (IOException e) {
                        Utils.LOGGER.error("Failed to load test layout: " + testName, e);
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                Utils.LOGGER.error("Failed to load test layouts", e);
                throw new RuntimeException(e);
            }
        }
        return layouts.stream().map(test -> new TestFunction(
            Utils.MOD_ID,
            Utils.MOD_ID+"/layouts/"+test.location(),
            "biomesquishertests:empty",
            Rotation.NONE,
            100,
            0L,
            true,
            1,
            1,
            context -> testLayout(context, test)
        )).toList();
    }

    private static MultiNoiseBiomeSource getOverworldBiomeList(Level level, ResourceManager resourceManager) {
        //noinspection DataFlowIssue
        var biomeParameters = ((SourceProvider) (Object) MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD).<Holder<Biome>>biomesquisher_test_apply(resourceKey ->
            level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(resourceKey)
        );
        var source = MultiNoiseBiomeSource.createFromList(biomeParameters);
        BiomeSquisher.squishBiomeSource(resourceManager, null, source, LevelStem.OVERWORLD, level.registryAccess());
        return source;
    }
}
