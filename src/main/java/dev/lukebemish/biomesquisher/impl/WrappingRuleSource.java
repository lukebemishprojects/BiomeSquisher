package dev.lukebemish.biomesquisher.impl;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;
import java.util.function.Function;

@Extend(targetClass = SurfaceRules.RuleSource.class, unsafe = false)
@Expose
public interface WrappingRuleSource extends SurfaceRules.RuleSource {
    ResourceLocation LOCATION = Utils.id("notapi_wrapping");
    KeyDispatchDataCodec<WrappingRuleSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(i -> i.group(
        SurfaceRules.RuleSource.CODEC.fieldOf("delegate").forGetter(WrappingRuleSource::delegate)
    ).apply(i, WrappingRuleSource::create)));

    @Constructor
    @SuppressWarnings("unused")
    static WrappingRuleSource create(@Field("delegate") @Field.Final SurfaceRules.RuleSource delegate) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

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
