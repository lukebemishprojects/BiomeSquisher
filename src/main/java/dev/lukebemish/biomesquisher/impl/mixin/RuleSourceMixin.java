package dev.lukebemish.biomesquisher.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.lukebemish.biomesquisher.impl.WrappingRuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceRules.RuleSource.class)
public interface RuleSourceMixin {
    @ModifyExpressionValue(
        method = "<clinit>()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
        )
    )
    private static Codec<SurfaceRules.RuleSource> biome_squisher$wrapCodec(Codec<SurfaceRules.RuleSource> original) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<SurfaceRules.RuleSource, T>> decode(DynamicOps<T> ops, T input) {
                return original.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(SurfaceRules.RuleSource input, DynamicOps<T> ops, T prefix) {
                if (input instanceof WrappingRuleSource wrapped) {
                    return encode(wrapped.delegate(), ops, prefix);
                }
                return original.encode(input, ops, prefix);
            }
        };
    }
}
