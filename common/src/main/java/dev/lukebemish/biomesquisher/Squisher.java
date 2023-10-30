package dev.lukebemish.biomesquisher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public record Squisher(Injection injection, Holder<Biome> biome, Relative.Series relative, boolean snap) {
    public static final Codec<Squisher> CODEC = RecordCodecBuilder.create(i -> i.group(
        Injection.CODEC.fieldOf("injection").forGetter(Squisher::injection),
        Biome.CODEC.fieldOf("biome").forGetter(Squisher::biome),
        Relative.Series.CODEC.optionalFieldOf("relative", Relative.DEFAULT).forGetter(Squisher::relative),
        Codec.BOOL.optionalFieldOf("snap", true).forGetter(Squisher::snap)
    ).apply(i, Squisher::new));
}
