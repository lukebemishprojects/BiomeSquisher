package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record NotConditionPredicate(ConditionPredicate predicate) implements ConditionPredicate {
    public static final MapCodec<NotConditionPredicate> CODEC = ConditionPredicate.CODEC.fieldOf("predicate").xmap(NotConditionPredicate::new, NotConditionPredicate::predicate);

    @Override
    public MapCodec<NotConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        return !predicate.matches(context, source);
    }
}
