package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record FindModifier(RuleFinder finder, RuleModifier modifier) implements RuleModifier {
    public static final MapCodec<FindModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RuleFinder.CODEC.fieldOf("finder").forGetter(FindModifier::finder),
            RuleModifier.CODEC.fieldOf("modifier").forGetter(FindModifier::modifier)
    ).apply(i, FindModifier::new));

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        return finder.find(source).apply(context, modifier);
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return CODEC;
    }
}
