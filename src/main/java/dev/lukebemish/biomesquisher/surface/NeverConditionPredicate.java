package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class NeverConditionPredicate implements ConditionPredicate {
    private NeverConditionPredicate() {}

    public static final NeverConditionPredicate INSTANCE = new NeverConditionPredicate();

    public static final MapCodec<NeverConditionPredicate> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<NeverConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        return false;
    }
}
