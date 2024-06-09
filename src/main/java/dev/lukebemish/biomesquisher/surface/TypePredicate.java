package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record TypePredicate(ResourceKey<MapCodec<? extends SurfaceRules.RuleSource>> key) implements RulePredicate {
    public static final MapCodec<TypePredicate> CODEC = ResourceKey.codec(Registries.MATERIAL_RULE).fieldOf("key").xmap(TypePredicate::new, TypePredicate::key);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        return BuiltInRegistries.MATERIAL_RULE.getResourceKey(ruleSource.codec().codec()).orElse(null) == key;
    }
}
