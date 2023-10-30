package dev.lukebemish.biomesquisher.test.fabric;

import dev.lukebemish.biomesquisher.test.BiomeSquisherGameTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class BiomeSquisherFabricGameTests {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void testLayouts(GameTestHelper context) {
        BiomeSquisherGameTests.testLayouts(context);
    }
}
