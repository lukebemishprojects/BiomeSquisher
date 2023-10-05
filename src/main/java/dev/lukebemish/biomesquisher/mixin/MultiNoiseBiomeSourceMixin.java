package dev.lukebemish.biomesquisher.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lukebemish.biomesquisher.injected.Squishable;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {

    @ModifyReturnValue(
        method = "collectPossibleBiomes",
        at = @At("RETURN")
    )
    private Stream<Holder<Biome>> biomesquisher_wrapBiomes(Stream<Holder<Biome>> biomes) {
        return Stream.concat(biomes, ((Squishable) ((MultiNoiseBiomeSourceAccessor) this).biomesquisher_parameters()).biomesquisher_squishers().possibleBiomes());
    }
}
