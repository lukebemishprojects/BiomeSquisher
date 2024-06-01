package dev.lukebemish.biomesquisher.test.mixin;

import dev.lukebemish.biomesquisher.test.SourceProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(targets = "net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList$Preset$SourceProvider")
public interface PresetProviderMixin extends SourceProvider {
    @Shadow
    <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function);

    @Override
    default <T> Climate.ParameterList<T> biomesquisher_test_apply(Function<ResourceKey<Biome>, T> function) {
        return apply(function);
    }
}
