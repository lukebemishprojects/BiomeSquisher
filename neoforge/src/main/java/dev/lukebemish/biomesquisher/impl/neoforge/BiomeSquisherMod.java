package dev.lukebemish.biomesquisher.impl.neoforge;

import com.mojang.serialization.Codec;
import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.Series;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisherCommands;
import dev.lukebemish.biomesquisher.impl.InternalScalingSampler;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Utils.MOD_ID)
public class BiomeSquisherMod {
    private static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Utils.MOD_ID);

    public BiomeSquisherMod() {
        BiomeSquisher.init();
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        var gameBus = NeoForge.EVENT_BUS;

        DENSITY_FUNCTION_TYPE.register(modBus);
        DENSITY_FUNCTION_TYPE.register(InternalScalingSampler.LOCATION.getPath(), InternalScalingSampler.CODEC::codec);

        modBus.addListener(DataPackRegistryEvent.NewRegistry.class, this::createDatapackRegistries);
        gameBus.addListener(RegisterCommandsEvent.class, this::registerCommands);
        gameBus.addListener(ServerStoppingEvent.class, event -> WebServerThread.stopServer());
    }

    private void createDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(BiomeSquisherRegistries.SERIES, Series.CODEC);
        event.dataPackRegistry(BiomeSquisherRegistries.SQUISHER, Squisher.CODEC);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BiomeSquisherCommands.register(event.getDispatcher());
    }
}
