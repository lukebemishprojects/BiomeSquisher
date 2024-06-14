package dev.lukebemish.biomesquisher.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.lukebemish.biomesquisher.impl.InternalScalingSampler;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DensityFunctions.class)
public class DensityFunctionsMixin {
    @ModifyExpressionValue(
        method = "<clinit>()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
        )
    )
    private static Codec<DensityFunction> biome_squisher$wrapCodec(Codec<DensityFunction> original) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<DensityFunction, T>> decode(DynamicOps<T> ops, T input) {
                return original.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(DensityFunction input, DynamicOps<T> ops, T prefix) {
                if (input instanceof InternalScalingSampler scaling) {
                    return encode(scaling.input(), ops, prefix);
                }
                return original.encode(input, ops, prefix);
            }
        };
    }
}
