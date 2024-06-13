package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record BiomeConditionPredicate(ResourceKey<Biome> key) implements ConditionPredicate {
    public static final MapCodec<BiomeConditionPredicate> CODEC = ResourceKey.codec(Registries.BIOME).fieldOf("key").xmap(BiomeConditionPredicate::new, BiomeConditionPredicate::key);

    @Override
    public MapCodec<BiomeConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        if (SurfaceRuleModifierUtils.isBiome(source)) {
            var predicate = SurfaceRuleModifierUtils.biomeNameTest(source);
            return predicate.test(key);
        } else {
            SurfaceRuleModifierUtils.warnOnNonBiome(context, source);
        }
        return false;
    }
}
