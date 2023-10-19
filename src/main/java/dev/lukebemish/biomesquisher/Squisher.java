package dev.lukebemish.biomesquisher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Squisher(Injection injection, Holder<Biome> biome, Relative.Series relative, boolean snap) {
    public static final Codec<Squisher> CODEC = RecordCodecBuilder.create(i -> i.group(
        Injection.CODEC.fieldOf("injection").forGetter(Squisher::injection),
        Biome.CODEC.fieldOf("biome").forGetter(Squisher::biome),
        Relative.Series.CODEC.optionalFieldOf("relative", Relative.DEFAULT).forGetter(Squisher::relative),
        Codec.BOOL.optionalFieldOf("snap", true).forGetter(Squisher::snap)
    ).apply(i, Squisher::new));

    public record Series(List<ResourceLocation> squishers, HolderSet<LevelStem> levels) {
        public static final Codec<Series> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.listOf().fieldOf("squishers").forGetter(Series::squishers),
            RegistryCodecs.homogeneousList(Registries.LEVEL_STEM, LevelStem.CODEC).fieldOf("levels").forGetter(Series::levels)
        ).apply(i, Series::new));
    }

    private static final String PREFIX = "biomesquisher";
    private static final String SQUISHERS_PREFIX = PREFIX + "/squishers";
    private static final String SERIES_PREFIX = PREFIX + "/series";
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public static void load(Holder<LevelStem> levelStem, Squishers squishers, ResourceManager resourceManager, RegistryOps<JsonElement> registryOps) {
        List<Pair<ResourceLocation, Series>> loaded = new ArrayList<>();
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(SERIES_PREFIX);
        var resources = fileToIdConverter.listMatchingResources(resourceManager);
        for (var entry : resources.entrySet()) {
            var location = entry.getKey();
            var resource = entry.getValue();
            try (var reader = resource.openAsReader()) {
                JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
                var result = Series.CODEC.parse(registryOps, jsonElement);
                if (result.result().isPresent()) {
                    var series = result.result().get();
                    if (series.levels.contains(levelStem)) {
                        loaded.add(Pair.of(location, series));
                    }
                } else if (result.error().isPresent()) {
                    Constants.LOGGER.error("Failed to parse biome squisher series from {}: {}", location, result.error().get());
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Failed to load biome squisher series from {}", location, e);
            }
        }
        loaded.sort(Comparator.comparing(p -> p.getFirst().toString()));
        for (var pair : loaded) {
            for (var squisherLocation : pair.getSecond().squishers) {
                var resource = resourceManager.getResource(squisherLocation.withPrefix(SQUISHERS_PREFIX+"/").withSuffix(".json"));
                if (resource.isPresent()) {
                    try (var reader = resource.get().openAsReader()) {
                        JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
                        var result = CODEC.parse(registryOps, jsonElement);
                        if (result.result().isPresent()) {
                            var squisher = result.result().get();
                            squishers.add(squisher);
                        } else if (result.error().isPresent()) {
                            Constants.LOGGER.error("Failed to parse biome squisher from {}: {}", squisherLocation, result.error().get());
                        }
                    } catch (IOException e) {
                        Constants.LOGGER.error("Failed to load biome squisher from {}", squisherLocation, e);
                    }
                } else {
                    Constants.LOGGER.error("Referenced biome squisher {} does not exist!", squisherLocation);
                }
            }
        }
    }
}
