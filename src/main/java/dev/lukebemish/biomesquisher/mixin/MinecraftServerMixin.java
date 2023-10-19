package dev.lukebemish.biomesquisher.mixin;

import com.mojang.datafixers.DataFixer;
import dev.lukebemish.biomesquisher.Constants;
import dev.lukebemish.biomesquisher.Squishers;
import dev.lukebemish.biomesquisher.injected.Squishable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void biomesquisher_load(
        Thread serverThread,
        LevelStorageSource.LevelStorageAccess storageSource,
        PackRepository packRepository,
        WorldStem worldStem,
        Proxy proxy,
        DataFixer fixerUpper,
        Services services,
        ChunkProgressListenerFactory progressListenerFactory,
        CallbackInfo ci
    ) {
        //noinspection DataFlowIssue
        var access = ((MinecraftServer) (Object) this).registryAccess();
        var registry = access.registry(Registries.LEVEL_STEM).orElseThrow();
        registry.forEach(value -> {
            ResourceKey<LevelStem> key = registry.getResourceKey(value).orElseThrow();
            Constants.LOGGER.info("Attempting to squish {}", key.location());
            Holder<LevelStem> holder = registry.getHolderOrThrow(key);
            if (value.generator() instanceof NoiseBasedChunkGenerator generator) {
                var biomeSource = generator.getBiomeSource();
                if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
                    var parameters = ((MultiNoiseBiomeSourceAccessor) multiNoiseBiomeSource).biomesquisher_parameters();
                    ((Squishable) parameters).biomesquisher_squish(holder, access, worldStem.resourceManager());
                    Squishers squishers = ((Squishable) parameters).biomesquisher_squishers();
                    if (squishers != null && squishers.needsSpacialScaling()) {
                        NoiseGeneratorSettings settings = generator.generatorSettings().value();
                        NoiseRouter router = settings.noiseRouter();
                        NoiseRouter newRouter = squishers.wrap(router);
                        @SuppressWarnings("deprecation") NoiseGeneratorSettings newSettings = new NoiseGeneratorSettings(
                            settings.noiseSettings(),
                            settings.defaultBlock(),
                            settings.defaultFluid(),
                            newRouter,
                            settings.surfaceRule(),
                            settings.spawnTarget(),
                            settings.seaLevel(),
                            settings.disableMobGeneration(),
                            settings.aquifersEnabled(),
                            settings.oreVeinsEnabled(),
                        settings.useLegacyRandomSource()
                        );
                        //noinspection DataFlowIssue
                        ((NoiseBasedChunkGeneratorAccessor) (Object) generator).biomesquisher_setGenerationSettings(Holder.direct(newSettings));
                    }
                }
            }
        });
    }
}
