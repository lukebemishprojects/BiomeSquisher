package dev.lukebemish.biomesquisher.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.injected.KnowsOriginalKey;
import dev.lukebemish.biomesquisher.surface.RuleModifier;
import dev.lukebemish.biomesquisher.surface.SurfaceRuleInjection;
import dev.lukebemish.opensesame.annotations.Coerce;
import dev.lukebemish.opensesame.annotations.extend.Constructor;
import dev.lukebemish.opensesame.annotations.extend.Extend;
import dev.lukebemish.opensesame.annotations.extend.Field;
import dev.lukebemish.opensesame.annotations.extend.Overrides;
import dev.lukebemish.opensesame.annotations.mixin.Expose;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Extend(targetClass = SurfaceRules.RuleSource.class, unsafe = false)
@Expose
public interface WrappingRuleSource extends SurfaceRules.RuleSource {
    ResourceLocation LOCATION = Utils.id("notapi_wrapping");
    KeyDispatchDataCodec<WrappingRuleSource> CODEC = KeyDispatchDataCodec.of(new MapCodec<>() {
        static final MapCodec<WrappingRuleSource> DELEGATE = RecordCodecBuilder.mapCodec(i -> i.group(
            SurfaceRules.RuleSource.CODEC.fieldOf("delegate").forGetter(WrappingRuleSource::delegate),
            ResourceKey.codec(Registries.NOISE_SETTINGS).fieldOf("generator").forGetter(WrappingRuleSource::generator)
        ).apply(i, WrappingRuleSource::create));

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return DELEGATE.keys(ops);
        }

        @Override
        public <T> DataResult<WrappingRuleSource> decode(DynamicOps<T> ops, MapLike<T> input) {
            return DELEGATE.decode(ops, input).map(it -> {
                if (ops instanceof NotifyingOps notifying) {
                    notifying.wrapped(it);
                }
                return it;
            });
        }

        @Override
        public <T> RecordBuilder<T> encode(WrappingRuleSource input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (ops instanceof NotifyingOps notifying) {
                notifying.wrapped(input);
            }
            return DELEGATE.encode(input, ops, prefix);
        }
    });

    static Codec<NoiseGeneratorSettings> wrap(Codec<NoiseGeneratorSettings> original) {
        return new Codec<>() {
            private static final String BIOMESQUISHER_GENERATOR_KEY = "biome_squisher_generator_key";
            private static final Codec<ResourceKey<NoiseGeneratorSettings>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.NOISE_SETTINGS);

            @SuppressWarnings("DataFlowIssue")
            @Override
            public <T> DataResult<Pair<NoiseGeneratorSettings, T>> decode(DynamicOps<T> ops, T input) {
                return original.decode(ops, input).flatMap(pair -> {
                    ops.getMap(input).result().ifPresent(mapLike -> {
                        var generatorKey = mapLike.get(BIOMESQUISHER_GENERATOR_KEY);
                        if (generatorKey != null) {
                            var key = RESOURCE_KEY_CODEC.parse(ops, generatorKey).result();
                            key.ifPresent(s -> ((KnowsOriginalKey) (Object) pair.getFirst()).biomesquisher_generatorKey(s));
                        }
                    });

                    return DataResult.success(pair);
                });
            }

            @SuppressWarnings("DataFlowIssue")
            @Override
            public <T> DataResult<T> encode(NoiseGeneratorSettings input, DynamicOps<T> ops, T prefix) {
                return original.encode(input, ops, prefix).flatMap(it -> {
                    var key = ((KnowsOriginalKey) (Object) input).biomesquisher_generatorKey();

                    if (key == null) {
                        // capture the key from a wrapped codec if it's null
                        //noinspection unchecked
                        ResourceKey<NoiseGeneratorSettings>[] keyHolder = new ResourceKey[1];
                        WrappingRuleSource.NotifyingOps.NotifyingJsonOps notifyingOps = WrappingRuleSource.NotifyingOps.NotifyingJsonOps.create(wrapped -> keyHolder[0] = wrapped.generator());
                        SurfaceRules.RuleSource.CODEC.encodeStart(notifyingOps, input.surfaceRule());
                        key = keyHolder[0];
                    }

                    if (key != null) {
                        return RESOURCE_KEY_CODEC.encode(key, ops, ops.empty()).flatMap(rk -> ops.mergeToMap(it, ops.createString(BIOMESQUISHER_GENERATOR_KEY), rk));
                    }
                    return DataResult.success(it);
                });
            }
        };
    }

    interface NotifyingOps {
        void wrapped(WrappingRuleSource source);

        class NotifyingJsonOps extends JsonOps implements NotifyingOps {
            private final Consumer<WrappingRuleSource> action;
            private boolean wrapped;

            protected NotifyingJsonOps(boolean compressed, Consumer<WrappingRuleSource> action) {
                super(compressed);
                this.action = action;
            }

            @Override
            public void wrapped(WrappingRuleSource source) {
                if (!this.wrapped) {
                    this.action.accept(source);
                    this.wrapped = true;
                }
            }

            public boolean isWrapped() {
                return wrapped;
            }

            public static NotifyingJsonOps create(Consumer<WrappingRuleSource> consumer) {
                return new NotifyingJsonOps(false, consumer);
            }
        }
    }

    @Constructor
    @SuppressWarnings("unused")
    static WrappingRuleSource create(@Field("delegate") @Field.Final SurfaceRules.RuleSource delegate, @Field("generator") @Field.Final ResourceKey<NoiseGeneratorSettings> generator) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    @Field("generator")
    ResourceKey<NoiseGeneratorSettings> generator();

    @Field("delegate")
    SurfaceRules.RuleSource delegate();

    @Field("modifiers")
    List<Holder<SurfaceRuleInjection>> modifiers();

    @Field("modifiers")
    void modifiers(List<Holder<SurfaceRuleInjection>> modifiers);

    @Field("actual")
    SurfaceRules.RuleSource actual();

    @Field("actual")
    void actual(SurfaceRules.RuleSource actual);

    @Overrides("codec")
    @SuppressWarnings("unused")
    default KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codecImpl() {
        return CODEC;
    }

    @Overrides("apply")
    @SuppressWarnings({"unused", "rawtypes", "unchecked"})
    default @Coerce(targetName = "net.minecraft.world.level.levelgen.SurfaceRules$SurfaceRule") Object applyImpl(@Coerce(targetName = "net.minecraft.world.level.levelgen.SurfaceRules$Context") Object context) {
        if (actual() == null) {
            modify();
        }
        return ((Function) actual()).apply(context);
    }

    default void modify() {
        synchronized (this) {
            if (actual() == null) {
                SurfaceRules.RuleSource source = delegate();
                for (Holder<SurfaceRuleInjection> modifier : modifiers()) {
                    //noinspection Convert2Lambda
                    RuleModifier.Context context = new RuleModifier.Context() {
                        @Override
                        public ResourceLocation modifierKey() {
                            return modifier.unwrapKey().orElseThrow(() -> new RuntimeException("Unbound holder ["+modifier+"]")).location();
                        }
                    };
                    source = modifier.value().modifier().apply(context, source);
                }
                actual(source);
            }
        }
    }

    @Overrides("apply")
    @SuppressWarnings("unused")
    default Object applyBridge(Object context) {
        return applyImpl(context);
    }
}
