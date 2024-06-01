package dev.lukebemish.biomesquisher.impl;

import net.minecraft.world.level.biome.Climate;

import java.util.EnumMap;
import java.util.Map;

public record Context(Climate.ParameterList<?> parameterList, Map<Dimension, Long> lowerBounds, Map<Dimension, Long> upperBounds, Map<Dimension, Double> quantization) {
    public static Context of(Climate.ParameterList<?> parameterList) {
        Map<Dimension, Long> lowerBounds = new EnumMap<>(Dimension.class);
        Map<Dimension, Long> upperBounds = new EnumMap<>(Dimension.class);
        Map<Dimension, Double> quantization = new EnumMap<>(Dimension.class);
        for (var dimension : Dimension.values()) {
            lowerBounds.put(dimension, Long.MAX_VALUE);
            upperBounds.put(dimension, Long.MIN_VALUE);
        }
        for (var parameter : parameterList.values()) {
            for (var dimension : Dimension.values()) {
                var lower = dimension.fromParameterPoint(parameter.getFirst()).min();
                var upper = dimension.fromParameterPoint(parameter.getFirst()).max();
                if (lower < lowerBounds.get(dimension)) {
                    lowerBounds.put(dimension, lower);
                }
                if (upper > upperBounds.get(dimension)) {
                    upperBounds.put(dimension, upper);
                }
            }
        }
        for (var dimension : Dimension.values()) {
            var diff = upperBounds.get(dimension) - lowerBounds.get(dimension);
            quantization.put(dimension, diff / 2.0);
        }
        return new Context(parameterList, lowerBounds, upperBounds, quantization);
    }
}
