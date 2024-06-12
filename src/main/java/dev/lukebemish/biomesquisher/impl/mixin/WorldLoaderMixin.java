package dev.lukebemish.biomesquisher.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldLoader.class)
public abstract class WorldLoaderMixin {
    @ModifyExpressionValue(
        method = "load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/WorldLoader;loadAndReplaceLayer(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/RegistryLayer;Ljava/util/List;)Lnet/minecraft/core/LayeredRegistryAccess;"
        )
    )
    private static LayeredRegistryAccess<RegistryLayer> biome_squisher$modifyWorldgenRegistries(
        LayeredRegistryAccess<RegistryLayer> original
    ) {
        var access = original.compositeAccess();
        var noiseSettingsRegistry = access.registry(Registries.NOISE_SETTINGS).orElseThrow();
        noiseSettingsRegistry.forEach(value -> {
            ResourceKey<NoiseGeneratorSettings> key = noiseSettingsRegistry.getResourceKey(value).orElseThrow();
            Utils.LOGGER.info("Modifying surface rules in {}", key.location());
            BiomeSquisher.setupSurfaceRuleModification(value, key);
            BiomeSquisher.modifySurfaceRules(value, access);
        });

        return original;
    }
}
