package dev.lukebemish.biomesquisher.test;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.biomesquisher.SquisherCollectionCallback;
import dev.lukebemish.biomesquisher.TargetTransformer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biomes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeSquisherTestMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("BiomeSquisherTestMod");

    @Override
    public void onInitialize() {
        SquisherCollectionCallback.EVENT.register((key, squishers, registryAccess) -> {
            TargetTransformer transformer = new TargetTransformer(
                0f, 0.2f,
                0.2f, 0.2f,
                0f, 0.3f,
                -0.3f, 0.3f,
                0f, 0.3f,
                -0.6f, 0f
            );
            squishers.add(Pair.of(
                transformer,
                registryAccess.registry(Registries.BIOME).orElseThrow().getHolderOrThrow(Biomes.WARPED_FOREST)
            ));
        });
        LOGGER.info("BiomeSquisherTestMod initialized");
    }
}
