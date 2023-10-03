package dev.lukebemish.biomesquisher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class BiomeSquisherMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
            BiomeDumpCommand.register(dispatcher, buildContext);
        });
    }
}
