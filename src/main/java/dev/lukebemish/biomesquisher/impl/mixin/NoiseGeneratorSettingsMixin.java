package dev.lukebemish.biomesquisher.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import dev.lukebemish.biomesquisher.impl.WrappingRuleSource;
import dev.lukebemish.biomesquisher.impl.injected.KnowsOriginalKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements KnowsOriginalKey {
    @Unique
    private ResourceKey<NoiseGeneratorSettings> biomesquisher_generatorKey;

    @Override
    public ResourceKey<NoiseGeneratorSettings> biomesquisher_generatorKey() {
        return biomesquisher_generatorKey;
    }

    @Override
    public synchronized void biomesquisher_generatorKey(ResourceKey<NoiseGeneratorSettings> key) {
        this.biomesquisher_generatorKey = key;
    }

    @ModifyExpressionValue(
        method = "<clinit>()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
        )
    )
    private static Codec<NoiseGeneratorSettings> biome_squisher$wrapCodec(Codec<NoiseGeneratorSettings> original) {
        return WrappingRuleSource.wrap(original);
    }
}
