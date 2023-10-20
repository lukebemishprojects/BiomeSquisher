package dev.lukebemish.biomesquisher.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.lukebemish.biomesquisher.Squisher;
import dev.lukebemish.biomesquisher.Squishers;
import dev.lukebemish.biomesquisher.injected.Squishable;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Climate.ParameterList.class)
public class ParameterListMixin implements Squishable {
    @Unique
    @Nullable private Squishers squishers;

    @Override
    public void biomesquisher_squish(ResourceKey<LevelStem> holder, RegistryAccess access, ResourceManager resourceManager) {
        //noinspection DataFlowIssue
        Squishers squishers = new Squishers((Climate.ParameterList<?>) (Object) this);
        Squisher.load(holder, squishers, access);
        this.squishers = squishers;
    }

    @Override
    public @Nullable Squishers biomesquisher_squishers() {
        return squishers;
    }

    @ModifyVariable(
        method = "findValueBruteForce",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Climate.TargetPoint biomesquisher_wrapFoundTargetPointBruteForce(Climate.TargetPoint targetPoint, @Share("biomesquishers_result") LocalRef<Holder<Biome>> result) {
        return biomesquisher_processTargetPoint(targetPoint, result);
    }

    @ModifyVariable(
        method = "findValueIndex(Lnet/minecraft/world/level/biome/Climate$TargetPoint;Lnet/minecraft/world/level/biome/Climate$DistanceMetric;)Ljava/lang/Object;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Climate.TargetPoint biomesquisher_wrapFoundTargetPoint(Climate.TargetPoint targetPoint, @Share("biomesquishers_result") LocalRef<Holder<Biome>> result) {
        return biomesquisher_processTargetPoint(targetPoint, result);
    }

    @Inject(
        method = "findValueBruteForce",
        at = @At("HEAD"),
        cancellable = true
    )
    private void biomesquisher_injectFoundTargetPointBruteForce(Climate.TargetPoint targetPoint, CallbackInfoReturnable<Object> cir, @Share("biomesquishers_result") LocalRef<Holder<Biome>> result) {
        if (result.get() != null) {
            cir.setReturnValue(result.get());
        }
    }

    @Inject(
        method = "findValueIndex(Lnet/minecraft/world/level/biome/Climate$TargetPoint;Lnet/minecraft/world/level/biome/Climate$DistanceMetric;)Ljava/lang/Object;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void biomesquisher_injectFoundTargetPoint(Climate.TargetPoint targetPoint, @Coerce Object distanceMetric, CallbackInfoReturnable<Object> cir, @Share("biomesquishers_result") LocalRef<Holder<Biome>> result) {
        if (result.get() != null) {
            cir.setReturnValue(result.get());
        }
    }

    private Climate.TargetPoint biomesquisher_processTargetPoint(Climate.TargetPoint targetPoint, LocalRef<Holder<Biome>> result) {
        if (this.squishers != null) {
            var either = this.squishers.apply(targetPoint);
            if (either.right().isPresent()) {
                result.set(either.right().get());
            }
            if (either.left().isPresent()) {
                return either.left().get();
            }
        }
        return targetPoint;
    }
}
