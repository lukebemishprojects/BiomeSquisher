package dev.lukebemish.biomesquisher.impl.injected;

import com.google.common.base.Supplier;

public class ResettableSupplier<T> implements Supplier<T> {
    private final Supplier<T> original;
    transient volatile boolean initialized;
    transient T value;

    public ResettableSupplier(Supplier<T> original) {
        this.original = original;
    }

    @Override
    public T get() {
        T existing = value;
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    T t = original.get();
                    value = t;
                    initialized = true;
                    return t;
                }
            }
        }
        return existing;
    }

    public void reset() {
        synchronized (this) {
            initialized = false;
            value = null;
        }
    }

    public interface Resettable {
        void biomesquisher$reset();
    }
}
