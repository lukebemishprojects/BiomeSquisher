package dev.lukebemish.biomesquisher.impl.fabric;

import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.Series;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisherCommands;
import dev.lukebemish.biomesquisher.impl.InternalScalingSampler;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
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
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
            BiomeSquisherCommands.register(dispatcher));
        DynamicRegistries.register(BiomeSquisherRegistries.SERIES, Series.CODEC);
        DynamicRegistries.register(BiomeSquisherRegistries.SQUISHER, Squisher.CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ServerLifecycleEvents.SERVER_STOPPING.register(server -> WebServerThread.stopServer());
        }
    }
}
