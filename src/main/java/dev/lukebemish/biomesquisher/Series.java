package dev.lukebemish.biomesquisher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.List;
import java.util.Set;

public record Series(List<ResourceKey<Squisher>> squishers, Set<ResourceKey<LevelStem>> levels) {
    public static final Codec<Series> CODEC = RecordCodecBuilder.create(i -> i.group(
        ResourceKey.codec(BiomeSquisherRegistries.SQUISHER).listOf().fieldOf("squishers").forGetter(Series::squishers),
        ResourceKey.codec(Registries.LEVEL_STEM).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("levels").forGetter(Series::levels)
    ).apply(i, Series::new));
}
