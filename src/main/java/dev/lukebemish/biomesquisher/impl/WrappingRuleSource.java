package dev.lukebemish.biomesquisher.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
                this.action.accept(source);
                this.wrapped = true;
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
    static WrappingRuleSource create(@Field("delegate") @Field.Final SurfaceRules.RuleSource delegate, @Field("generator") ResourceKey<NoiseGeneratorSettings> generator) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    @Field("generator")
    ResourceKey<NoiseGeneratorSettings> generator();

    @Field("generator")
    void generator(ResourceKey<NoiseGeneratorSettings> generator);

    @Field("delegate")
    @Field.Final
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
