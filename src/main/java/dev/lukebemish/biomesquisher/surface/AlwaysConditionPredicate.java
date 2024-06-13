package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class AlwaysConditionPredicate implements ConditionPredicate {
    private AlwaysConditionPredicate() {}

    public static final AlwaysConditionPredicate INSTANCE = new AlwaysConditionPredicate();

    public static final MapCodec<AlwaysConditionPredicate> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends ConditionPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(RuleModifier.Context context, SurfaceRules.ConditionSource source) {
        return true;
    }
}
