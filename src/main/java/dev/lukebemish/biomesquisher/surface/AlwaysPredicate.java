package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class AlwaysPredicate implements RulePredicate {
    private AlwaysPredicate() {}

    public static final AlwaysPredicate INSTANCE = new AlwaysPredicate();

    public static final MapCodec<AlwaysPredicate> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends RulePredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.RuleSource ruleSource) {
        return true;
    }
}
