package dev.lukebemish.biomesquisher.surface;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record ReplaceModifier(SurfaceRules.RuleSource source) implements RuleModifier {
    public static final MapCodec<ReplaceModifier> CODEC = SurfaceRules.RuleSource.CODEC.fieldOf("source").xmap(ReplaceModifier::new, ReplaceModifier::source);

    @Override
    public SurfaceRules.RuleSource apply(Context context, SurfaceRules.RuleSource source) {
        return source;
    }

    @Override
    public MapCodec<? extends RuleModifier> codec() {
        return CODEC;
    }
}
