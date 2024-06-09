package dev.lukebemish.biomesquisher.impl.neoforge;

import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import dev.lukebemish.biomesquisher.Series;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.BiomeSquisherCommands;
import dev.lukebemish.biomesquisher.impl.InternalScalingSampler;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.impl.server.WebServerThread;
import dev.lukebemish.biomesquisher.impl.SurfaceModifierBootstrap;
import dev.lukebemish.biomesquisher.surface.SurfaceRuleInjection;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

@Mod(Utils.MOD_ID)
public class BiomeSquisherMod {
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Utils.MOD_ID);

    static final List<Registry<?>> NEW_REGISTRIES = List.of(
        BiomeSquisherRegistries.SURFACE_MODIFIER_TYPES,
        BiomeSquisherRegistries.SURFACE_PREDICATE_TYPES,
        BiomeSquisherRegistries.SURFACE_FINDER_TYPES
    );

    public BiomeSquisherMod(IEventBus modBus) {
        BiomeSquisher.init();

        DENSITY_FUNCTION_TYPE.register(modBus);
        DENSITY_FUNCTION_TYPE.register(InternalScalingSampler.LOCATION.getPath(), InternalScalingSampler.CODEC::codec);

        modBus.addListener(DataPackRegistryEvent.NewRegistry.class, this::createDatapackRegistries);
        modBus.addListener(NewRegistryEvent.class, this::addRegistries);
        modBus.addListener(RegisterEvent.class, this::registerCodecs);
        modBus.addListener(FMLCommonSetupEvent.class, this::commonSetup);
    }

    private void createDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(BiomeSquisherRegistries.SERIES, Series.CODEC);
        event.dataPackRegistry(BiomeSquisherRegistries.SQUISHER, Squisher.CODEC);
        event.dataPackRegistry(BiomeSquisherRegistries.SURFACE_RULE_INJECTION, SurfaceRuleInjection.CODEC);
    }

    private void addRegistries(NewRegistryEvent event) {
        NEW_REGISTRIES.forEach(event::register);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        BiomeSquisherCommands.register(event.getDispatcher());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Let's have reproducible ordering, thank you very much
        var gameBus = NeoForge.EVENT_BUS;

        event.enqueueWork(() -> {
            gameBus.addListener(RegisterCommandsEvent.class, this::registerCommands);

            if (FMLEnvironment.dist == Dist.CLIENT) {
                gameBus.addListener(ServerStoppingEvent.class, e -> WebServerThread.stopServer());
            }
        });
    }

    private void registerCodecs(RegisterEvent event) {
        event.register(BiomeSquisherRegistries.SURFACE_MODIFIER_TYPES_KEY, helper ->
            SurfaceModifierBootstrap.modifiers((s, c) -> helper.register(Utils.id(s), c))
        );
        event.register(BiomeSquisherRegistries.SURFACE_PREDICATE_TYPES_KEY, helper ->
            SurfaceModifierBootstrap.predicates((s, c) -> helper.register(Utils.id(s), c))
        );
        event.register(BiomeSquisherRegistries.SURFACE_FINDER_TYPES_KEY, helper ->
            SurfaceModifierBootstrap.finders((s, c) -> helper.register(Utils.id(s), c))
        );
    }
}
