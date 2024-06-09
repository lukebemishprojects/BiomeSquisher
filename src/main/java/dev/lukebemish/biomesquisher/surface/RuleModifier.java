package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.BiomeSquisherRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.Function;

public interface RuleModifier extends RuleMutator {
    Codec<RuleModifier> CODEC = BiomeSquisherRegistries.SURFACE_MODIFIER_TYPES.byNameCodec()
        .dispatch(RuleModifier::codec, Function.identity());

    @Override
    SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source);

    MapCodec<? extends RuleModifier> codec();

    interface Context {
        ResourceLocation modifierKey();
    }
}
