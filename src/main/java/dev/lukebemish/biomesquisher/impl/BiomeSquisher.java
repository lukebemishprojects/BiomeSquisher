package dev.lukebemish.biomesquisher.impl;

import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.impl.injected.KnowsOriginalKey;
import dev.lukebemish.biomesquisher.impl.injected.Squishable;
import dev.lukebemish.biomesquisher.impl.mixin.MultiNoiseBiomeSourceAccessor;
import dev.lukebemish.biomesquisher.impl.mixin.NoiseBasedChunkGeneratorAccessor;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import dev.lukebemish.biomesquisher.surface.SurfaceRuleInjection;
import dev.lukebemish.opensesame.annotations.Open;
import dev.lukebemish.opensesame.annotations.mixin.UnFinal;
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

    public static void setupOriginalKeyAwareGenerators(NoiseGeneratorSettings generator, ResourceKey<NoiseGeneratorSettings> key) {
        //noinspection DataFlowIssue
        ((KnowsOriginalKey) (Object) generator).biomesquisher_generatorKey(key);
    }

    public static void modifySurfaceRules(NoiseGeneratorSettings generator, RegistryAccess access, ResourceKey<NoiseGeneratorSettings> backupKey) {
        var surfaceRulesSource = generator.surfaceRule();
        //noinspection DataFlowIssue
        var key = ((KnowsOriginalKey) (Object) generator).biomesquisher_generatorKey();
        WrappingRuleSource.NotifyingOps.NotifyingJsonOps ops = WrappingRuleSource.NotifyingOps.NotifyingJsonOps.create(wrapped -> wrapped.modifiers(loadRuleModifiers(key == null ? wrapped.generator() : key, access)));
        SurfaceRules.RuleSource.CODEC.encodeStart(ops, surfaceRulesSource);
        if (!ops.isWrapped()) {
            var realKey = key == null ? backupKey : key;
            var wrappedSource = WrappingRuleSource.create(surfaceRulesSource, realKey);
            wrappedSource.modifiers(loadRuleModifiers(realKey, access));
            setSurfaceRule(generator, wrappedSource);
        }
    }

    @SuppressWarnings("unused")
    @Open(
        targetClass = NoiseGeneratorSettings.class,
        name = "surfaceRule",
        type = Open.Type.SET_INSTANCE
    )
    @UnFinal
    private static void setSurfaceRule(NoiseGeneratorSettings settings, SurfaceRules.RuleSource source) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    private static List<Holder<SurfaceRuleInjection>> loadRuleModifiers(ResourceKey<NoiseGeneratorSettings> settingsKey, RegistryAccess registryAccess) {
        List<Holder<SurfaceRuleInjection>> loaded = new ArrayList<>();
        for (var entry : registryAccess.registry(BiomeSquisherRegistries.SURFACE_RULE_INJECTION).orElseThrow(() -> new IllegalStateException("Missing surface rule injection registry!")).asHolderIdMap()) {
            if (entry.value().generators().contains(settingsKey)) {
                loaded.add(entry);
            }
        }
        loaded.sort(Comparator.comparing(p -> p.unwrapKey().orElseThrow().location().toString()));
        return loaded;
    }
}
