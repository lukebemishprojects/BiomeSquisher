package dev.lukebemish.biomesquisher;

import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.impl.Platform;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.biomesquisher.surface.ConditionPredicate;
import dev.lukebemish.biomesquisher.surface.RuleFinder;
import dev.lukebemish.biomesquisher.surface.RuleModifier;
import dev.lukebemish.biomesquisher.surface.RulePredicate;
import dev.lukebemish.biomesquisher.surface.SurfaceRuleInjection;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class BiomeSquisherRegistries {
    private BiomeSquisherRegistries() {}

    public static final ResourceKey<Registry<Series>> SERIES = ResourceKey.createRegistryKey(Utils.id("series"));
    public static final ResourceKey<Registry<Squisher>> SQUISHER = ResourceKey.createRegistryKey(Utils.id("squisher"));
    public static final ResourceKey<Registry<SurfaceRuleInjection>> SURFACE_RULE_INJECTION = ResourceKey.createRegistryKey(Utils.id("surface_rule_injection"));

    public static final ResourceKey<Registry<MapCodec<? extends RuleModifier>>> SURFACE_MODIFIER_TYPES_KEY = ResourceKey.createRegistryKey(Utils.id("surface_modifier_types"));
    public static final ResourceKey<Registry<MapCodec<? extends RulePredicate>>> SURFACE_PREDICATE_TYPES_KEY = ResourceKey.createRegistryKey(Utils.id("surface_predicate_types"));
    public static final ResourceKey<Registry<MapCodec<? extends ConditionPredicate>>> SURFACE_CONDITION_PREDICATE_TYPES_KEY = ResourceKey.createRegistryKey(Utils.id("surface_predicate_types"));
    public static final ResourceKey<Registry<MapCodec<? extends RuleFinder>>> SURFACE_FINDER_TYPES_KEY = ResourceKey.createRegistryKey(Utils.id("surface_finder_types"));

    public static final Registry<MapCodec<? extends RuleModifier>> SURFACE_MODIFIER_TYPES = Platform.INSTANCE.registry(SURFACE_MODIFIER_TYPES_KEY);
    public static final Registry<MapCodec<? extends RulePredicate>> SURFACE_PREDICATE_TYPES = Platform.INSTANCE.registry(SURFACE_PREDICATE_TYPES_KEY);
    public static final Registry<MapCodec<? extends ConditionPredicate>> SURFACE_CONDITION_PREDICATE_TYPES = Platform.INSTANCE.registry(SURFACE_CONDITION_PREDICATE_TYPES_KEY);
    public static final Registry<MapCodec<? extends RuleFinder>> SURFACE_FINDER_TYPES = Platform.INSTANCE.registry(SURFACE_FINDER_TYPES_KEY);
}
