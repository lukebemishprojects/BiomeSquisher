package dev.lukebemish.biomesquisher.surface;

import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.BiFunction;

public interface RuleMutator extends BiFunction<RuleModifier.Context, SurfaceRules.RuleSource, SurfaceRules.RuleSource> {
}
