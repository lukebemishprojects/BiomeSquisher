package dev.lukebemish.biomesquisher.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.opensesame.annotations.Coerce;
import dev.lukebemish.opensesame.annotations.Open;
import dev.lukebemish.opensesame.annotations.extend.Constructor;
import dev.lukebemish.opensesame.annotations.extend.Extend;
import dev.lukebemish.opensesame.annotations.extend.Field;
import dev.lukebemish.opensesame.annotations.extend.Overrides;
import dev.lukebemish.opensesame.annotations.mixin.Expose;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.stream.Stream;

@Extend(targetClass = SurfaceRules.RuleSource.class, unsafe = false)
@Expose
interface WrappingRuleSource {
    KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> CODEC = KeyDispatchDataCodec.of(new MapCodec<>() {
        static final MapCodec<SurfaceRules.RuleSource> DELEGATE = RecordCodecBuilder.mapCodec(i -> i.group(
            SurfaceRules.RuleSource.CODEC.fieldOf("delegate").forGetter(r -> ((WrappingRuleSource) r).delegate())
        ).apply(i, d -> (SurfaceRules.RuleSource) create(d)));

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            if (ops instanceof NotifyingOps notifying) {
                notifying.wrapped();
            }
            return DELEGATE.keys(ops);
        }

        @Override
        public <T> DataResult<SurfaceRules.RuleSource> decode(DynamicOps<T> ops, MapLike<T> input) {
            if (ops instanceof NotifyingOps notifying) {
                notifying.wrapped();
            }
            return DELEGATE.decode(ops, input);
        }

        @Override
        public <T> RecordBuilder<T> encode(SurfaceRules.RuleSource input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (ops instanceof NotifyingOps notifying) {
                notifying.wrapped();
            }
            return DELEGATE.encode(input, ops, prefix);
        }
    });

    interface NotifyingOps {
        void wrapped();

        class NotifyingJsonOps extends JsonOps implements NotifyingOps {
            private boolean notified;

            protected NotifyingJsonOps(boolean compressed) {
                super(compressed);
            }

            @Override
            public void wrapped() {
                this.notified = true;
            }

            boolean isWrapped() {
                return this.notified;
            }

            public static NotifyingJsonOps create() {
                return new NotifyingJsonOps(false);
            }
        }
    }

    @Constructor
    @SuppressWarnings("unused")
    static WrappingRuleSource create(@Field("delegate") SurfaceRules.RuleSource delegate) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    @Field("delegate")
    SurfaceRules.RuleSource delegate();

    @Overrides("codec")
    default KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }

    @Overrides("apply")
    @SuppressWarnings("unused")
    default @Coerce(targetName = "net.minecraft.world.level.levelgen.SurfaceRules$SurfaceRule") Object apply(@Coerce(targetName = "net.minecraft.world.level.levelgen.SurfaceRules$Context") Object context) {
        return accessApply(delegate(), context);
    }

    @Open(targetClass = SurfaceRules.RuleSource.class, name = "apply", type = Open.Type.VIRTUAL)
    @SuppressWarnings("unused")
    private static @Coerce(targetName = "net.minecraft.world.level.levelgen.SurfaceRules$SurfaceRule") Object accessApply(SurfaceRules.RuleSource source, @Coerce(targetName = "net.minecraft.world.level.levelgen.SurfaceRules$Context") Object context) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }
}
