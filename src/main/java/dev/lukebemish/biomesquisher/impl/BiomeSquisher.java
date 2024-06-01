package dev.lukebemish.biomesquisher.impl;

import dev.lukebemish.biomesquisher.impl.injected.Squishable;
import dev.lukebemish.biomesquisher.impl.mixin.MultiNoiseBiomeSourceAccessor;
import dev.lukebemish.biomesquisher.impl.mixin.NoiseBasedChunkGeneratorAccessor;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jetbrains.annotations.Nullable;

public final class BiomeSquisher {
    private BiomeSquisher() {}

    public static void init() {
        if (Platform.INSTANCE.isClient()) {
            Runtime.getRuntime().addShutdownHook(new Thread(WebServerThread::waitOnStopServer));
        }
    }

    public static void squishBiomeSource(ResourceManager resourceManager, @Nullable NoiseBasedChunkGenerator generator, MultiNoiseBiomeSource multiNoiseBiomeSource, ResourceKey<LevelStem> key, RegistryAccess access) {
        var parameters = ((MultiNoiseBiomeSourceAccessor) multiNoiseBiomeSource).biomesquisher_parameters();
        ((Squishable) parameters).biomesquisher_squish(key, access, resourceManager);
        Squishers squishers = ((Squishable) parameters).biomesquisher_squishers();
        if (generator != null && squishers != null && squishers.needsSpacialScaling()) {
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
