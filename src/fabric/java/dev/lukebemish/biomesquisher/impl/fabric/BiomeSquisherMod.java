package dev.lukebemish.biomesquisher.impl.fabric;

import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.Series;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisherCommands;
import dev.lukebemish.biomesquisher.impl.InternalScalingSampler;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.impl.WrappingRuleSource;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import dev.lukebemish.biomesquisher.impl.SurfaceModifierBootstrap;
import dev.lukebemish.biomesquisher.surface.SurfaceRuleInjection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class BiomeSquisherMod implements ModInitializer {

    @Override
    public void onInitialize() {
        BiomeSquisher.init();
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, InternalScalingSampler.LOCATION, InternalScalingSampler.CODEC.codec());
        Registry.register(BuiltInRegistries.MATERIAL_RULE, WrappingRuleSource.LOCATION, WrappingRuleSource.CODEC.codec());
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
            BiomeSquisherCommands.register(dispatcher));
        DynamicRegistries.register(BiomeSquisherRegistries.SERIES, Series.CODEC);
        DynamicRegistries.register(BiomeSquisherRegistries.SQUISHER, Squisher.CODEC);
        DynamicRegistries.register(BiomeSquisherRegistries.SURFACE_RULE_INJECTION, SurfaceRuleInjection.CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ServerLifecycleEvents.SERVER_STOPPING.register(server -> WebServerThread.stopServer());
        }

        SurfaceModifierBootstrap.modifiers((s, c) -> Registry.register(BiomeSquisherRegistries.SURFACE_MODIFIER_TYPES, Utils.id(s), c));
        SurfaceModifierBootstrap.predicates((s, c) -> Registry.register(BiomeSquisherRegistries.SURFACE_PREDICATE_TYPES, Utils.id(s), c));
        SurfaceModifierBootstrap.conditionPredicates((s, c) -> Registry.register(BiomeSquisherRegistries.SURFACE_CONDITION_PREDICATE_TYPES, Utils.id(s), c));
        SurfaceModifierBootstrap.finders((s, c) -> Registry.register(BiomeSquisherRegistries.SURFACE_FINDER_TYPES, Utils.id(s), c));
    }
}
