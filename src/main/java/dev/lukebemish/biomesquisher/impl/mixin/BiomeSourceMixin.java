package dev.lukebemish.biomesquisher.impl.mixin;

import com.google.common.base.Supplier;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lukebemish.biomesquisher.impl.injected.ResettableSupplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin implements ResettableSupplier.Resettable {
    @Unique
    ResettableSupplier<Set<Holder<Biome>>> biomesquisher$biomes;

    @WrapOperation(
        method = "<init>()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/base/Suppliers;memoize(Lcom/google/common/base/Supplier;)Lcom/google/common/base/Supplier;"
        )
    )
    Supplier<Set<Holder<Biome>>> biomesquisher$resettable(Supplier<Set<Holder<Biome>>> original, Operation<Supplier<Set<Holder<Biome>>>> operation) {
        biomesquisher$biomes = new ResettableSupplier<>(() -> operation.call(original).get());
        return biomesquisher$biomes;
    }

    @Override
    public void biomesquisher$reset() {
        if (biomesquisher$biomes == null) {
            return;
        }
        biomesquisher$biomes.reset();
    }
}
