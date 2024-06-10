package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class ThenRunFinder implements RuleFinder {
    public static final ThenRunFinder INSTANCE = new ThenRunFinder();
    public static final MapCodec<ThenRunFinder> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public ModificationView find() {
        return (c, m, source) -> {
            if (SurfaceRuleModifierUtils.isTest(source)) {
                var ifTrue = SurfaceRuleModifierUtils.ifTrue(source);
                var thenRun = SurfaceRuleModifierUtils.thenRun(source);
                return SurfaceRules.ifTrue(ifTrue, m.apply(c, thenRun));
            } else {
                SurfaceRuleModifierUtils.warnOnNonTest(c, source);
            }
            return source;
        };
    }

    @Override
    public MapCodec<? extends RuleFinder> codec() {
        return CODEC;
    }
}
