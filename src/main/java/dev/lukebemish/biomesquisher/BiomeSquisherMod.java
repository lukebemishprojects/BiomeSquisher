package dev.lukebemish.biomesquisher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class BiomeSquisherMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, Constants.id("notapi_scaling"), InternalScalingSampler.CODEC.codec());
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
            BiomeSquisherCommands.register(dispatcher, buildContext));
        DynamicRegistries.register(Constants.SERIES_KEY, Squisher.Series.CODEC);
        DynamicRegistries.register(Constants.SQUISHER_KEY, Squisher.CODEC);
    }
}
