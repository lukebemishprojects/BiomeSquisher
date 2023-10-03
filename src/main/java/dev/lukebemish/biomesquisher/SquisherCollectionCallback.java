package dev.lukebemish.biomesquisher;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.RegistryAccess;

public interface SquisherCollectionCallback {

    Event<SquisherCollectionCallback> EVENT = EventFactory.createArrayBacked(SquisherCollectionCallback.class, calbacks -> (squishers, registryAccess) -> {
        for (SquisherCollectionCallback callback : calbacks) {
            callback.collect(squishers, registryAccess);
        }
    });

    void collect(Squishers squishers, RegistryAccess registryAccess);
}
