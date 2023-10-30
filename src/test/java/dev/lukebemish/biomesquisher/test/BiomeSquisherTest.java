package dev.lukebemish.biomesquisher.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class BiomeSquisherTest implements ModInitializer {
    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new TestLayoutReloadListener());
    }
}
