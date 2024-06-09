package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;

public record SequenceModifier(List<RuleModifier> modifiers) implements RuleModifier {
    public static final MapCodec<SequenceModifier> CODEC = RuleModifier.CODEC.listOf().fieldOf("modifiers").xmap(SequenceModifier::new, SequenceModifier::modifiers);

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        for (RuleModifier modifier : modifiers) {
            source = modifier.apply(context, source);
        }
        return source;
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return CODEC;
    }
}
