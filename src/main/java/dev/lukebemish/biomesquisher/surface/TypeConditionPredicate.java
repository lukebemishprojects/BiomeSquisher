package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record TypeConditionPredicate(ResourceKey<MapCodec<? extends SurfaceRules.ConditionSource>> key) implements ConditionPredicate {
    public static final MapCodec<TypeConditionPredicate> CODEC = ResourceKey.codec(Registries.MATERIAL_CONDITION).fieldOf("key").xmap(TypeConditionPredicate::new, TypeConditionPredicate::key);

    @Override
    public MapCodec<TypeConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        return BuiltInRegistries.MATERIAL_CONDITION.getResourceKey(source.codec().codec()).orElse(null) == key;
    }
}
