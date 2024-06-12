package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public record PrependModifier(SurfaceRules.RuleSource source) implements RuleModifier {
    public static final MapCodec<PrependModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        SurfaceRules.RuleSource.CODEC.fieldOf("source").forGetter(PrependModifier::source)
    ).apply(i, PrependModifier::new));

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        if (SurfaceRuleModifierUtils.isSequence(source)) {
            List<SurfaceRules.RuleSource> sources = new ArrayList<>();
            sources.add(this.source);
            sources.addAll(SurfaceRuleModifierUtils.sequence(source));
            return SurfaceRuleModifierUtils.createSequence(sources);
        } else {
            List<SurfaceRules.RuleSource> sources = new ArrayList<>();
            sources.add(this.source);
            sources.add(source);
            return SurfaceRuleModifierUtils.createSequence(sources);
        }
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return CODEC;
    }
}
