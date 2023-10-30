package dev.lukebemish.biomesquisher.impl;

import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.Series;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.impl.injected.Squishable;
import dev.lukebemish.biomesquisher.impl.mixin.MultiNoiseBiomeSourceAccessor;
import dev.lukebemish.biomesquisher.impl.mixin.NoiseBasedChunkGeneratorAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jetbrains.annotations.Nullable;

public class BiomeSquisherMod implements ModInitializer {
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

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, Utils.id("notapi_scaling"), InternalScalingSampler.CODEC.codec());
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
            BiomeSquisherCommands.register(dispatcher));
        DynamicRegistries.register(BiomeSquisherRegistries.SERIES, Series.CODEC);
        DynamicRegistries.register(BiomeSquisherRegistries.SQUISHER, Squisher.CODEC);
    }
}
