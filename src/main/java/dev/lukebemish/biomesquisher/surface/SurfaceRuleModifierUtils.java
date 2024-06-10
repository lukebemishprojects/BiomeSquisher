package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import dev.lukebemish.biomesquisher.impl.Utils;
import dev.lukebemish.opensesame.annotations.Open;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

final class SurfaceRuleModifierUtils {
    private SurfaceRuleModifierUtils() {}

    static final ResourceKey<MapCodec<? extends SurfaceRules.RuleSource>> SEQUENCE = ResourceKey.create(Registries.MATERIAL_RULE, new ResourceLocation("sequence"));
    static final ResourceKey<MapCodec<? extends SurfaceRules.RuleSource>> TEST = ResourceKey.create(Registries.MATERIAL_RULE, new ResourceLocation("condition"));

    static boolean isSequence(SurfaceRules.RuleSource source) {
        return BuiltInRegistries.MATERIAL_RULE.getResourceKey(source.codec().codec()).orElse(null) == SEQUENCE;
    }

    @Open(
        targetName = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRuleSource",
        name = "sequence",
        type = Open.Type.VIRTUAL
    )
    @SuppressWarnings("unused")
    static List<SurfaceRules.RuleSource> sequence(SurfaceRules.RuleSource sequence) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    @Open(
        targetName = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRuleSource",
        name = "<init>",
        type = Open.Type.CONSTRUCT
    )@SuppressWarnings("unused")

    static SurfaceRules.RuleSource createSequence(List<SurfaceRules.RuleSource> sources) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    static void warnOnNonSequence(RuleModifier.Context context, SurfaceRules.RuleSource source) {
        Utils.LOGGER.warn("In surface rule modifier {}, expected minecraft:sequence rule source, got {}", context.modifierKey(), source);
    }

    static boolean isTest(SurfaceRules.RuleSource source) {
        return BuiltInRegistries.MATERIAL_RULE.getResourceKey(source.codec().codec()).orElse(null) == TEST;
    }

    @Open(
        targetName = "net.minecraft.world.level.levelgen.SurfaceRules$TestRuleSource",
        name = "thenRun",
        type = Open.Type.VIRTUAL
    )
    @SuppressWarnings("unused")
    static SurfaceRules.RuleSource thenRun(SurfaceRules.RuleSource sequence) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    @Open(
        targetName = "net.minecraft.world.level.levelgen.SurfaceRules$TestRuleSource",
        name = "ifTrue",
        type = Open.Type.VIRTUAL
    )
    @SuppressWarnings("unused")
    static SurfaceRules.ConditionSource ifTrue(SurfaceRules.RuleSource sequence) {
        throw new UnsupportedOperationException("Replaced by OpenSesame at compile time");
    }

    static void warnOnNonTest(RuleModifier.Context context, SurfaceRules.RuleSource source) {
        Utils.LOGGER.warn("In surface rule modifier {}, expected minecraft:condition rule source, got {}", context.modifierKey(), source);
    }
}
