package dev.lukebemish.biomesquisher.test.mixin;

import dev.lukebemish.biomesquisher.test.SourceProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(MultiNoiseBiomeSourceParameterList.Preset.class)
public class PresetMixin implements SourceProvider {
    @Unique
    private SourceProvider delegate;

    @Override
    public <T> Climate.ParameterList<T> biomesquisher_test_apply(Function<ResourceKey<Biome>, T> function) {
        return delegate.biomesquisher_test_apply(function);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureProvider(ResourceLocation id, @Coerce Object provider, CallbackInfo ci) {
        this.delegate = (SourceProvider) provider;
    }
}
