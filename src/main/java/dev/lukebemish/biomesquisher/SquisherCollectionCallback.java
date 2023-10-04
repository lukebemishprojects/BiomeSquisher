package dev.lukebemish.biomesquisher;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

public interface SquisherCollectionCallback {

    Event<SquisherCollectionCallback> EVENT = EventFactory.createArrayBacked(SquisherCollectionCallback.class, callbacks -> (key, squishers, registryAccess) -> {
        for (SquisherCollectionCallback callback : callbacks) {
            callback.collect(key, squishers, registryAccess);
        }
    });

    void collect(ResourceKey<LevelStem> key, Squishers squishers, RegistryAccess registryAccess);
}
