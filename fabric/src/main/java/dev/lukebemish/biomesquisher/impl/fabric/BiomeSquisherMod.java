package dev.lukebemish.biomesquisher.impl.fabric;

import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.Series;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisherCommands;
import dev.lukebemish.biomesquisher.impl.InternalScalingSampler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class BiomeSquisherMod implements ModInitializer {

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, InternalScalingSampler.LOCATION, InternalScalingSampler.CODEC.codec());
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
            BiomeSquisherCommands.register(dispatcher));
        DynamicRegistries.register(BiomeSquisherRegistries.SERIES, Series.CODEC);
        DynamicRegistries.register(BiomeSquisherRegistries.SQUISHER, Squisher.CODEC);
    }
}
