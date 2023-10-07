package dev.lukebemish.biomesquisher.test;

import dev.lukebemish.biomesquisher.DimensionBehaviour;
import dev.lukebemish.biomesquisher.Injection;
import dev.lukebemish.biomesquisher.Relative;
import dev.lukebemish.biomesquisher.SquisherCollectionCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BiomeSquisherTestMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("BiomeSquisherTestMod");

    @Override
    public void onInitialize() {
        SquisherCollectionCallback.EVENT.register((key, squishers, registryAccess) -> {
            if (key == LevelStem.OVERWORLD) {
                squishers.add(
                    new Injection(
                        new DimensionBehaviour.Squish(0.22f),
                        new DimensionBehaviour.Squish(0.2f),
                        new DimensionBehaviour.Range(0.5f, 1.0f),
                        new DimensionBehaviour.Squish(-0.3f),
                        new DimensionBehaviour.Range(-0.6f, 0f),
                        new DimensionBehaviour.Squish(0.2f),
                        0.1f
                    ),
                    registryAccess.registry(Registries.BIOME).orElseThrow().getHolderOrThrow(Biomes.WARPED_FOREST),
                    new Relative.Series(List.of(new Relative(
                        Relative.Position.START,
                        Relative.Position.START,
                        Relative.Position.START,
                        Relative.Position.START
                    )))
                );

                /*
                squishers.add(
                    new Injection(
                        new DimensionBehaviour.Squish(0.2f),
                        new DimensionBehaviour.Squish(0.2f),
                        new DimensionBehaviour.Range(0.5f, 1.0f),
                        new DimensionBehaviour.Squish(-0.3f),
                        new DimensionBehaviour.Range(-0.6f, 0f),
                        new DimensionBehaviour.Squish(0.2f),
                        0.1f
                    ),
                    registryAccess.registry(Registries.BIOME).orElseThrow().getHolderOrThrow(Biomes.SOUL_SAND_VALLEY),
                    new Relative.Series(List.of(
                        new Relative(
                            Relative.Position.START,
                            Relative.Position.CENTER,
                            Relative.Position.CENTER,
                            Relative.Position.CENTER
                        ),
                        new Relative(
                            Relative.Position.CENTER,
                            Relative.Position.START,
                            Relative.Position.START,
                            Relative.Position.START
                        )
                    ))
                );

                squishers.add(
                    new Injection(
                        new DimensionBehaviour.Squish(0.2f),
                        new DimensionBehaviour.Squish(-0.6f),
                        new DimensionBehaviour.Range(0.5f, 1.0f),
                        new DimensionBehaviour.Squish(-0.3f),
                        new DimensionBehaviour.Range(-0.6f, 0f),
                        new DimensionBehaviour.Squish(0.2f),
                        0.1f
                    ),
                    registryAccess.registry(Registries.BIOME).orElseThrow().getHolderOrThrow(Biomes.CRIMSON_FOREST),
                    new Relative.Series(List.of(new Relative(
                        Relative.Position.START,
                        Relative.Position.START,
                        Relative.Position.START,
                        Relative.Position.START
                    )))
                );
                */
            }
        });
        LOGGER.info("BiomeSquisherTestMod initialized");
    }
}
