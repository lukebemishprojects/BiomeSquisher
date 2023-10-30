package dev.lukebemish.biomesquisher.test;

import dev.lukebemish.biomesquisher.impl.BiomeSquisher;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.impl.dump.BiomeDumper;
import dev.lukebemish.biomesquisher.impl.dump.PngOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.dimension.LevelStem;

import java.io.IOException;

public class BiomeSquisherGameTests {

    public static void testLayouts(GameTestHelper context) {
        var multiNoiseBiomeSource = getOverworldBiomeList(context.getLevel(), context.getLevel().getServer().getResourceManager());
        for (LayoutTest test : TestLayoutReloadListener.LAYOUTS) {
            int[][] data = new int[1024][];
            try {
                BiomeDumper.dump(context.getLevel(), multiNoiseBiomeSource, test.specs().x(), test.specs().y(), test.specs().slice(), (level, biomeGetter, posibleBiomes) ->
                    PngOutput.INSTANCE.dumpImage(biomeGetter, posibleBiomes, i -> new int[1024], (i, row) -> data[i] = row, (row, col, v) -> row[col] = v));
            } catch (IOException e) {
                Utils.LOGGER.error("Failed to save biome dump", e);
                context.fail("Failed to save biome dump");
                return;
            }
            context.assertTrue(test.target().equals(new LayoutTest.Layout(data)), "Biome layout "+test.location()+" does not match expected layout.");
        }
        context.succeed();
    }

    private static MultiNoiseBiomeSource getOverworldBiomeList(Level level, ResourceManager resourceManager) {
        //noinspection DataFlowIssue
        var biomeParameters = ((SourceProvider) (Object) MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD).<Holder<Biome>>biomesquisher_test_apply(resourceKey ->
            level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(resourceKey)
        );
        var source = MultiNoiseBiomeSource.createFromList(biomeParameters);
        BiomeSquisher.squishBiomeSource(resourceManager, null, source, LevelStem.OVERWORLD, level.registryAccess());
        return source;
    }
}
