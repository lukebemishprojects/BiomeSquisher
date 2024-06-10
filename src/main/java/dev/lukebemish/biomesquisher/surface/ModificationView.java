package dev.lukebemish.biomesquisher.surface;

import net.minecraft.world.level.levelgen.SurfaceRules;

public interface ModificationView {
    SurfaceRules.RuleSource apply(RuleModifier.Context context, RuleMutator modifier, SurfaceRules.RuleSource source);

    static ModificationView simple() {
        return (context, modifier, source) -> source;
    }
}
