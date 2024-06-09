package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.Set;

public record SurfaceRuleInjection(RuleModifier modifier, Set<ResourceKey<NoiseGeneratorSettings>> generators) {
    public static final Codec<SurfaceRuleInjection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        RuleModifier.CODEC.fieldOf("modifier").forGetter(SurfaceRuleInjection::modifier),
        ResourceKey.codec(Registries.NOISE_SETTINGS).listOf().xmap(Set::copyOf, List::copyOf).fieldOf("generators").forGetter(SurfaceRuleInjection::generators)
    ).apply(instance, SurfaceRuleInjection::new));
}
