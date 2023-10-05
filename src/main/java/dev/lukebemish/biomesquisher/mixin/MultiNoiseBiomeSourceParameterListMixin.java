package dev.lukebemish.biomesquisher.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lukebemish.biomesquisher.injected.Squishable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.stream.Stream;

@Mixin(MultiNoiseBiomeSourceParameterList.class)
public abstract class MultiNoiseBiomeSourceParameterListMixin {
    @ModifyReturnValue(
        method = "usedBiomes",
        at = @At("RETURN")
    )
    private Stream<ResourceKey<Biome>> biomesquisher_wrapBiomes(Stream<ResourceKey<Biome>> biomes) {
        return Stream.concat(biomes,
            ((Squishable) this.biomesquisher_parameters()).biomesquisher_squishers().possibleBiomes().map(h -> h.unwrapKey().orElse(null)).filter(Objects::nonNull)
        ).distinct();
    }

    @Accessor("parameters")
    abstract Climate.ParameterList<Holder<Biome>> biomesquisher_parameters();
}
