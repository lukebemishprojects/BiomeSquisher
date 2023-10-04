package dev.lukebemish.biomesquisher.mixin;

import dev.lukebemish.biomesquisher.Constants;
import dev.lukebemish.biomesquisher.injected.Squishable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    // TODO: find a shared server/client entrypoint (this one is client only)
    @Inject(
        method = "load",
        at = @At("RETURN")
    )
    private static void biomesquisher_load(ResourceManager resourceManager, RegistryAccess registryAccess, List<RegistryDataLoader.RegistryData<?>> registryData, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        if (registryData.stream().anyMatch(data -> data.key() == Registries.LEVEL_STEM)) {
            var access = cir.getReturnValue();
            var registry = access.registry(Registries.LEVEL_STEM).orElseThrow();
            registry.forEach(value -> {
                ResourceKey<LevelStem> key = registry.getResourceKey(value).orElseThrow();
                Constants.LOGGER.info("Attempting to squish {}", key.location());
                var biomeSource = value.generator().getBiomeSource();
                if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
                    var parameters = ((MultiNoiseBiomeSourceAccessor) multiNoiseBiomeSource).biomesquisher_parameters();
                    ((Squishable) parameters).biomesquisher_squish(key, access);
                }
            });
        }
    }
}
