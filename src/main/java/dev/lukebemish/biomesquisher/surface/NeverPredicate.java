package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class NeverPredicate implements RulePredicate {
    private NeverPredicate() {}

    public static final NeverPredicate INSTANCE = new NeverPredicate();

    public static final MapCodec<NeverPredicate> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        return false;
    }
}
