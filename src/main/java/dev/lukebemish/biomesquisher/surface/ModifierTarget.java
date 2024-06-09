package dev.lukebemish.biomesquisher.surface;

import net.minecraft.world.level.levelgen.SurfaceRules;

public interface ModifierTarget {
    SurfaceRules.RuleSource apply(RuleModifier.Context context, RuleMutator modifier);

    static ModifierTarget simple(SurfaceRules.RuleSource source) {
        return (context, modifier) -> source;
    }
}
