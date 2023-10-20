package dev.lukebemish.biomesquisher;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public record Squisher(Injection injection, Holder<Biome> biome, Relative.Series relative, boolean snap) {
    public static final Codec<Squisher> CODEC = RecordCodecBuilder.create(i -> i.group(
        Injection.CODEC.fieldOf("injection").forGetter(Squisher::injection),
        Biome.CODEC.fieldOf("biome").forGetter(Squisher::biome),
        Relative.Series.CODEC.optionalFieldOf("relative", Relative.DEFAULT).forGetter(Squisher::relative),
        Codec.BOOL.optionalFieldOf("snap", true).forGetter(Squisher::snap)
    ).apply(i, Squisher::new));

    public record Series(List<ResourceKey<Squisher>> squishers, Set<ResourceKey<LevelStem>> levels) {
        public static final Codec<Series> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Constants.SQUISHER_KEY).listOf().fieldOf("squishers").forGetter(Series::squishers),
            ResourceKey.codec(Registries.LEVEL_STEM).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("levels").forGetter(Series::levels)
        ).apply(i, Series::new));
    }

    public static void load(ResourceKey<LevelStem> level, Squishers squishers, RegistryAccess registryAccess) {
        List<Pair<ResourceLocation, Series>> loaded = new ArrayList<>();
        for (var entry : registryAccess.registry(Constants.SERIES_KEY).orElseThrow(() -> new IllegalStateException("Missing series registry!")).entrySet()) {
            if (entry.getValue().levels().contains(level)) {
                loaded.add(Pair.of(entry.getKey().location(), entry.getValue()));
            }
        }
        loaded.sort(Comparator.comparing(p -> p.getFirst().toString()));
        var squishersRegistry = registryAccess.registry(Constants.SQUISHER_KEY).orElseThrow(() -> new IllegalStateException("Missing squisher registry!"));
        for (var pair : loaded) {
            for (var squisherLocation : pair.getSecond().squishers()) {
                var squisher = squishersRegistry.get(squisherLocation);
                if (squisher != null) {
                    squishers.add(squisher);
                } else {
                    Constants.LOGGER.error("Referenced biome squisher {} does not exist!", squisherLocation);
                }
            }
        }
    }
}
