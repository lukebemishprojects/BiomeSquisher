package dev.lukebemish.biomesquisher.impl.mixin;

import com.mojang.datafixers.DataFixer;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.Utils;
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
            if (value.generator() instanceof NoiseBasedChunkGenerator generator) {
                var biomeSource = generator.getBiomeSource();
                if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
                    Utils.LOGGER.info("Squishing biomes in {}", key.location());
                    BiomeSquisher.squishBiomeSource(worldStem.resourceManager(), generator, multiNoiseBiomeSource, key, access);
                } else {
                    Utils.LOGGER.info("Not squishing {}; not a MultiNoiseBiomeSource", key.location());
                }
            } else {
                Utils.LOGGER.info("Not squishing {}; not a NoiseBasedChunkGenerator", key.location());
            }
        });
    }
}
