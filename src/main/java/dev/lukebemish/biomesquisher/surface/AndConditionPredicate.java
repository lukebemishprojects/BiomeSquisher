package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record AndConditionPredicate(List<ConditionPredicate> predicates) implements ConditionPredicate {
    public static final MapCodec<AndConditionPredicate> CODEC = ConditionPredicate.CODEC.listOf().fieldOf("predicates").xmap(AndConditionPredicate::new, AndConditionPredicate::predicates);

    @Override
    public MapCodec<AndConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        return predicates.stream().allMatch(p -> p.matches(context, source));
    }
}
