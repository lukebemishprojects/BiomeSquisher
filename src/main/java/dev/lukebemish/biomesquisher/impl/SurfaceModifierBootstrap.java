package dev.lukebemish.biomesquisher.impl;

import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.surface.AfterModifier;
import dev.lukebemish.biomesquisher.surface.AllPredicate;
import dev.lukebemish.biomesquisher.surface.AlwaysPredicate;
import dev.lukebemish.biomesquisher.surface.BeforeModifier;
import dev.lukebemish.biomesquisher.surface.ManyFinder;
import dev.lukebemish.biomesquisher.surface.AndPredicate;
import dev.lukebemish.biomesquisher.surface.AnyPredicate;
import dev.lukebemish.biomesquisher.surface.AppendModifier;
import dev.lukebemish.biomesquisher.surface.ChainFinder;
import dev.lukebemish.biomesquisher.surface.FindModifier;
import dev.lukebemish.biomesquisher.surface.MatchingFinder;
import dev.lukebemish.biomesquisher.surface.MatchingModifier;
import dev.lukebemish.biomesquisher.surface.NeverPredicate;
import dev.lukebemish.biomesquisher.surface.NotPredicate;
import dev.lukebemish.biomesquisher.surface.OrPredicate;
import dev.lukebemish.biomesquisher.surface.CheckFinder;
import dev.lukebemish.biomesquisher.surface.PrependModifier;
import dev.lukebemish.biomesquisher.surface.ReplaceConditionModifier;
import dev.lukebemish.biomesquisher.surface.ReplaceModifier;
import dev.lukebemish.biomesquisher.surface.RuleFinder;
import dev.lukebemish.biomesquisher.surface.RuleModifier;
import dev.lukebemish.biomesquisher.surface.RulePredicate;
import dev.lukebemish.biomesquisher.surface.SequenceModifier;
import dev.lukebemish.biomesquisher.surface.ThenRunFinder;
import dev.lukebemish.biomesquisher.surface.TypePredicate;

import java.util.function.BiConsumer;

public final class SurfaceModifierBootstrap {
    private SurfaceModifierBootstrap() {}

    public static void modifiers(BiConsumer<String, MapCodec<? extends RuleModifier>> consumer) {
        consumer.accept("append", AppendModifier.CODEC);
        consumer.accept("prepend", PrependModifier.CODEC);
        consumer.accept("matching", MatchingModifier.CODEC);
        consumer.accept("after", AfterModifier.CODEC);
        consumer.accept("before", BeforeModifier.CODEC);
        consumer.accept("find", FindModifier.CODEC);
        consumer.accept("replace", ReplaceModifier.CODEC);
        consumer.accept("replace_condition", ReplaceConditionModifier.CODEC);
        consumer.accept("sequence", SequenceModifier.CODEC);
    }

    public static void predicates(BiConsumer<String, MapCodec<? extends RulePredicate>> consumer) {
        consumer.accept("and", AndPredicate.CODEC);
        consumer.accept("or", OrPredicate.CODEC);
        consumer.accept("not", NotPredicate.CODEC);
        consumer.accept("always", AlwaysPredicate.CODEC);
        consumer.accept("never", NeverPredicate.CODEC);
        consumer.accept("any", AnyPredicate.CODEC);
        consumer.accept("all", AllPredicate.CODEC);
        consumer.accept("type", TypePredicate.CODEC);
    }

    public static void finders(BiConsumer<String, MapCodec<? extends RuleFinder>> consumer) {
        consumer.accept("chain", ChainFinder.CODEC);
        consumer.accept("many", ManyFinder.CODEC);
        consumer.accept("check", CheckFinder.CODEC);
        consumer.accept("matching", MatchingFinder.CODEC);
        consumer.accept("then_run", ThenRunFinder.CODEC);
    }
}
