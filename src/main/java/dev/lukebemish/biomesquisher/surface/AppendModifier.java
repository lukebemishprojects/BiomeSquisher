package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.List;

public record AppendModifier(SurfaceRules.RuleSource source) implements RuleModifier {
    public static final MapCodec<AppendModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        SurfaceRules.RuleSource.CODEC.fieldOf("source").forGetter(AppendModifier::source)
    ).apply(i, AppendModifier::new));

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        if (SurfaceRuleModifierUtils.isSequence(source)) {
            List<SurfaceRules.RuleSource> sources = new ArrayList<>(SurfaceRuleModifierUtils.sequence(source));
            sources.add(this.source);
            return SurfaceRuleModifierUtils.createSequence(sources);
        } else {
            SurfaceRuleModifierUtils.warnOnNonSequence(context, source);
        }
        return source;
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return null;
    }
}
