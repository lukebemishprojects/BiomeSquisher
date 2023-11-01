package dev.lukebemish.biomesquisher.test.fabric;

import dev.lukebemish.biomesquisher.test.TestLayoutReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class BiomeSquisherTest implements ModInitializer {
    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiedTestLayoutReloadListener());
    }

    private static final class IdentifiedTestLayoutReloadListener extends TestLayoutReloadListener implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return TestLayoutReloadListener.LOCATION;
        }
    }
}
