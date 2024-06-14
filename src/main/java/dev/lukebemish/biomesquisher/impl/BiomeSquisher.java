package dev.lukebemish.biomesquisher.impl;

import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.impl.injected.Squishable;
import dev.lukebemish.biomesquisher.impl.mixin.MultiNoiseBiomeSourceAccessor;
import dev.lukebemish.biomesquisher.impl.mixin.NoiseBasedChunkGeneratorAccessor;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import dev.lukebemish.biomesquisher.surface.SurfaceRuleInjection;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public static void modifySurfaceRules(NoiseBasedChunkGenerator generator, ResourceKey<LevelStem> key, RegistryAccess access) {
        var modifiers = loadRuleModifiers(key, access);
        if (!modifiers.isEmpty()) {
            Utils.LOGGER.info("Injecting surface rules in {}", key.location());
            NoiseGeneratorSettings settings = generator.generatorSettings().value();
            SurfaceRules.RuleSource original = settings.surfaceRule();
            SurfaceRules.RuleSource wrapped = wrapRule(original, access, key, modifiers);
            @SuppressWarnings("deprecation") NoiseGeneratorSettings newSettings = new NoiseGeneratorSettings(
                settings.noiseSettings(),
                settings.defaultBlock(),
                settings.defaultFluid(),
                settings.noiseRouter(),
                wrapped,
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

    private static SurfaceRules.RuleSource wrapRule(SurfaceRules.RuleSource original, RegistryAccess access, ResourceKey<LevelStem> key, List<Holder<SurfaceRuleInjection>> modifiers) {
        var wrappedSource = WrappingRuleSource.create(original);
        wrappedSource.modifiers(modifiers);
        return wrappedSource;
    }

    private static List<Holder<SurfaceRuleInjection>> loadRuleModifiers(ResourceKey<LevelStem> settingsKey, RegistryAccess registryAccess) {
        List<Holder<SurfaceRuleInjection>> loaded = new ArrayList<>();
        for (var entry : registryAccess.registry(BiomeSquisherRegistries.SURFACE_RULE_INJECTION).orElseThrow(() -> new IllegalStateException("Missing surface rule injection registry!")).asHolderIdMap()) {
            if (entry.value().levels().contains(settingsKey)) {
                loaded.add(entry);
            }
        }
        loaded.sort(Comparator.comparing(p -> p.unwrapKey().orElseThrow().location().toString()));
        return loaded;
    }
}
