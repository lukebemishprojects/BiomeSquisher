package dev.lukebemish.biomesquisher.test;

import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestLayoutReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final ResourceLocation LOCATION = new ResourceLocation("biomesquisher", "test_layout");
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    @Override
    public ResourceLocation getFabricId() {
        return LOCATION;
    }

    public static final List<LayoutTest> LAYOUTS = new ArrayList<>();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        String name = Utils.MOD_ID+"/tests";
        FileToIdConverter fileToIdConverterJson = new FileToIdConverter(name, ".json");
        FileToIdConverter fileToIdConverterPng = new FileToIdConverter(name, ".png");
        resourceManager.listResources(name, rl -> rl.getPath().endsWith(".json")).forEach((rl, resource) -> {
            Resource png = resourceManager.getResource(fileToIdConverterPng.idToFile(fileToIdConverterJson.fileToId(rl))).orElse(null);
            if (png == null) {
                Utils.LOGGER.error("Missing png for test layout: "+rl);
                return;
            }
            try (var pngStream = png.open();
                 var jsonReader = resource.openAsReader()) {
                PngReader r = new PngReader(pngStream);
                if (r.imgInfo.cols != 1024 || r.imgInfo.rows != 1024) {
                    Utils.LOGGER.error("Invalid png size for test layout: "+rl);
                    return;
                }
                JsonElement json = GSON.fromJson(jsonReader, JsonElement.class);
                LayoutTest.LayoutSpecs specs = LayoutTest.LayoutSpecs.CODEC.parse(JsonOps.INSTANCE, json).mapError(e -> {
                    Utils.LOGGER.error("Failed to parse test layout {}: {}", rl, e);
                    return e;
                }).result().orElse(null);
                if (specs == null) return;
                int[][] data = new int[1024][1024];
                for (int i = 0; i < 1024; i++) {
                    int[] row = data[i];
                    ImageLineInt rRow = (ImageLineInt) r.readRow(i);
                    for (int j = 0; j < 1024; j++) {
                        row[j] = rRow.getElem(j);
                    }
                }
                LAYOUTS.add(new LayoutTest(fileToIdConverterJson.fileToId(rl), specs, new LayoutTest.Layout(data)));
            } catch (IOException e) {
                Utils.LOGGER.error("Failed to load test layout: "+rl, e);
            }
        });
    }
}
