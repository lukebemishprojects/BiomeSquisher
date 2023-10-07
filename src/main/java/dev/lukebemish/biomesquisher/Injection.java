package dev.lukebemish.biomesquisher;

import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class Injection {
    private final DimensionBehaviour temperature;
    private final DimensionBehaviour humidity;
    private final DimensionBehaviour.Range continentalness;
    private final DimensionBehaviour erosion;
    private final DimensionBehaviour.Range depth;
    private final DimensionBehaviour weirdness;
    private final float radius;

    // derived
    private final DimensionBehaviour[] behaviours;
    private final int squishCount;
    private final int[] squishIndices;
    private final int rangeCount;
    private final int[] rangeIndices;

    public Injection(
        DimensionBehaviour temperature,
        DimensionBehaviour humidity,
        DimensionBehaviour.Range continentalness,
        DimensionBehaviour erosion,
        DimensionBehaviour.Range depth,
        DimensionBehaviour weirdness,
        float radius
    ) {
        int totalSquish = 0;
        int totalRange = 2;
        if (temperature.asSquish() != null) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (humidity.asSquish() != null) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (erosion.asSquish() != null) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (weirdness.asSquish() != null) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (totalSquish < 2) {
            throw new IllegalArgumentException("Must have at least 2 squish dimensions");
        }

        this.temperature = temperature;
        this.humidity = humidity;
        this.continentalness = continentalness;
        this.erosion = erosion;
        this.depth = depth;
        this.weirdness = weirdness;
        this.radius = radius;
        this.behaviours = new DimensionBehaviour[] {
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness
        };
        squishCount = totalSquish;
        squishIndices = new int[squishCount];
        for (int i = 0, j = 0; i < behaviours.length; i++) {
            if (behaviours[i].asSquish() != null) {
                squishIndices[j] = i;
                j += 1;
            }
        }
        rangeCount = totalRange;
        rangeIndices = new int[rangeCount];
        for (int i = 0, j = 0; i < behaviours.length; i++) {
            if (behaviours[i].asRange() != null) {
                rangeIndices[j] = i;
                j += 1;
            }
        }
    }

    public DimensionBehaviour temperature() {
        return temperature;
    }

    public DimensionBehaviour humidity() {
        return humidity;
    }

    public DimensionBehaviour.Range continentalness() {
        return continentalness;
    }

    public DimensionBehaviour erosion() {
        return erosion;
    }

    public DimensionBehaviour.Range depth() {
        return depth;
    }

    public DimensionBehaviour weirdness() {
        return weirdness;
    }

    public float radius() {
        return radius;
    }

    private static float unquantizeAndClamp(long coord) {
        return Mth.clamp(Climate.unquantizeCoord(coord), -1, 1);
    }

    public @NotNull Climate.TargetPoint unsquish(Climate.TargetPoint initial, Relative.Series relatives) {
        float[] thePoint = new float[] {
            unquantizeAndClamp(initial.temperature()),
            unquantizeAndClamp(initial.humidity()),
            unquantizeAndClamp(initial.continentalness()),
            unquantizeAndClamp(initial.erosion()),
            unquantizeAndClamp(initial.depth()),
            unquantizeAndClamp(initial.weirdness())
        };

        float[] toUnsquish = new float[squishCount];
        float[] toUnsquishCenters = new float[squishCount];
        float[] diffs = new float[squishCount];
        for (int i = 0; i < squishCount; i++) {
            float o = thePoint[squishIndices[i]];
            toUnsquish[i] = o;
            //noinspection DataFlowIssue
            float c = behaviours[squishIndices[i]].asSquish().position();
            toUnsquishCenters[i] = c;
            diffs[i] = c - o;
        }

        float[] relativeEdge = new float[squishCount];
        int temperature = -1;
        int humidity = -1;
        int erosion = -1;
        int weirdness = -1;
        for (int i = 0, j = 0; i < behaviours.length; i++) {
            if (behaviours[i].asSquish() != null) {
                switch (i) {
                    case 0 -> temperature = j;
                    case 1 -> humidity = j;
                    case 3 -> erosion = j;
                    case 5 -> weirdness = j;
                }
                j++;
            }
        }
        for (var relative : relatives.relatives()) {
            boolean toBreak = false;
            if (temperature != -1) {
                var position = relative.temperature();
                if (relativeEdge[temperature] == 0) {
                    relativeEdge[temperature] = position.offset();
                }
                if (position != Relative.Position.CENTER) {
                    toBreak = true;
                }
            }
            if (humidity != -1) {
                var position = relative.humidity();
                if (relativeEdge[humidity] == 0) {
                    relativeEdge[humidity] = position.offset();
                }
                if (position != Relative.Position.CENTER) {
                    toBreak = true;
                }
            }
            if (erosion != -1) {
                var position = relative.erosion();
                if (relativeEdge[erosion] == 0) {
                    relativeEdge[erosion] = position.offset();
                }
                if (position != Relative.Position.CENTER) {
                    toBreak = true;
                }
            }
            if (weirdness != -1) {
                var position = relative.weirdness();
                if (relativeEdge[weirdness] == 0) {
                    relativeEdge[weirdness] = position.offset();
                }
                if (position != Relative.Position.CENTER) {
                    toBreak = true;
                }
            }
            if (toBreak) {
                break;
            }
        }

        float multiplier = 1;

        for (int i = 0; i < rangeCount; i++) {
            float p = thePoint[rangeIndices[i]];
            DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
            if (p < range.min()) {
                //multiplier *= (p + 1) / (range.min() + 1);
                float total = range.min() + 1;
                float partial = range.min() - p;
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            } else if (p > range.max()) {
                //multiplier *= (1 - p) / (1 - range.max());
                float total = 1 - range.max();
                float partial = p - range.max();
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            }
        }

        float distSquare = Float.MAX_VALUE;
        for (int i = 0; i < squishCount; i++) {
            float diff = diffs[i];
            if (diff < 0) {
                float time = (toUnsquish[i] - 1) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = 1;
                    } else {
                        scaled[j] = toUnsquish[j] - diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toUnsquishCenters, scaled));
            } else if (diff > 0) {
                float time = (1 + toUnsquish[i]) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = -1;
                    } else {
                        scaled[j] = toUnsquish[j] - diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toUnsquishCenters, scaled));
            }
        }

        float relativeDistance;
        float distanceToCenter = Mth.sqrt(distanceSquare(toUnsquish, toUnsquishCenters));

        if (distSquare == Float.MAX_VALUE) {
            relativeDistance = 0;
        } else {
            float distanceTotal = Mth.sqrt(distSquare);
            relativeDistance = distanceToCenter / distanceTotal;
        }

        if (multiplier == 0) {
            return initial;
        }

        float smallRadius = radius * (1 - multiplier);

        if (relativeDistance < smallRadius && relativeDistance != 0) {
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                float diff = diffs[i];
                thePoint[squishIndices[i]] = toUnsquishCenters[i] - diff / (1 - multiplier);
            }

            return climateOf(thePoint);
        }

        double relativeVolume = Math.pow(radius, squishCount);
        double relativeSmallVolume = Math.pow(smallRadius, squishCount);
        float finalDist = (float) Math.pow(
            (1 - relativeVolume) * (Math.pow(relativeDistance, squishCount) - relativeSmallVolume) / (1 - relativeSmallVolume) + relativeVolume,
            1.0 / squishCount
        );

        if (relativeDistance == 0) {
            float[] direction = subtract(relativeEdge, toUnsquishCenters);
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                float diff = direction[i];
                thePoint[squishIndices[i]] = toUnsquishCenters[i] + diff * finalDist;
            }

            return climateOf(thePoint);
        }

        float movedDistRatio = (relativeDistance - finalDist) / relativeDistance;

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            float diff = diffs[i];
            thePoint[squishIndices[i]] += diff * movedDistRatio;
        }

        return climateOf(thePoint);
    }

    private static float[] subtract(float[] a, float[] b) {
        float[] out = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = a[i] - b[i];
        }
        return out;
    }

    public @Nullable Climate.TargetPoint squish(Climate.TargetPoint initial) {
        float[] thePoint = new float[] {
            unquantizeAndClamp(initial.temperature()),
            unquantizeAndClamp(initial.humidity()),
            unquantizeAndClamp(initial.continentalness()),
            unquantizeAndClamp(initial.erosion()),
            unquantizeAndClamp(initial.depth()),
            unquantizeAndClamp(initial.weirdness())
        };

        float[] toSquish = new float[squishCount];
        float[] toSquishCenters = new float[squishCount];
        float[] diffs = new float[squishCount];
        for (int i = 0; i < squishCount; i++) {
            float o = thePoint[squishIndices[i]];
            toSquish[i] = o;
            //noinspection DataFlowIssue
            float c = behaviours[squishIndices[i]].asSquish().position();
            toSquishCenters[i] = c;
            diffs[i] = c - o;
        }

        float distSquare = Float.MAX_VALUE;
        for (int i = 0; i < squishCount; i++) {
            float diff = diffs[i];
            if (diff < 0) {
                float time = (toSquish[i] - 1) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = 1;
                    } else {
                        scaled[j] = toSquish[j] - diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toSquishCenters, scaled));
            } else if (diff > 0) {
                float time = (1 + toSquish[i]) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = -1;
                    } else {
                        scaled[j] = toSquish[j] - diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toSquishCenters, scaled));
            }
        }

        float relativeDistance;

        if (distSquare == Float.MAX_VALUE) {
            relativeDistance = 0;
        } else {
            float distanceTotal = Mth.sqrt(distSquare);
            float distanceToCenter = Mth.sqrt(distanceSquare(toSquish, toSquishCenters));
            relativeDistance = distanceToCenter / distanceTotal;
        }

        if (relativeDistance < radius) {
            boolean isInRange = true;
            for (int i = 0; i < rangeCount; i++) {
                @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
                float min = range.min();
                float max = range.max();
                float value = thePoint[rangeIndices[i]];
                if (value < min || value > max) {
                    isInRange = false;
                }
            }
            if (isInRange) {
                return null;
            }
        }

        float multiplier = 1;

        for (int i = 0; i < rangeCount; i++) {
            float p = thePoint[rangeIndices[i]];
            DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
            if (p < range.min()) {
                //multiplier *= (p + 1) / (range.min() + 1);
                float total = range.min() + 1;
                float partial = range.min() - p;
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            } else if (p > range.max()) {
                //multiplier *= (1 - p) / (1 - range.max());
                float total = 1 - range.max();
                float partial = p - range.max();
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            }
        }

        if (relativeDistance < radius) {
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                float diff = diffs[i];
                thePoint[squishIndices[i]] += diff * multiplier;
            }
            return climateOf(thePoint);
        }

        float smallRadius = radius * (1 - multiplier);
        double relativeVolume = Math.pow(radius, squishCount);
        double relativeSmallVolume = Math.pow(smallRadius, squishCount);
        double finalDist = Math.pow(
            (1 - relativeSmallVolume) * (Math.pow(relativeDistance, squishCount) - relativeVolume) / (1 - relativeVolume) + relativeSmallVolume,
            1.0 / squishCount
        );
        float movedDistRatio = (float) (relativeDistance - finalDist) / relativeDistance;

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            float diff = diffs[i];
            thePoint[squishIndices[i]] += diff * movedDistRatio;
        }

        return climateOf(thePoint);
    }

    private static Climate.TargetPoint climateOf(float[] out) {
        return new Climate.TargetPoint(
            Climate.quantizeCoord(out[0]),
            Climate.quantizeCoord(out[1]),
            Climate.quantizeCoord(out[2]),
            Climate.quantizeCoord(out[3]),
            Climate.quantizeCoord(out[4]),
            Climate.quantizeCoord(out[5])
        );
    }

    private static float distanceSquare(float[] a, float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    Injection scale(float totalVolume) {
        return new Injection(
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness,
            (float) (radius / Math.pow(totalVolume, 1.0 / squishCount)));
    }

    @Override
    public String toString() {
        return "Injection{" +
            "temperature=" + temperature +
            ", humidity=" + humidity +
            ", continentalness=" + continentalness +
            ", erosion=" + erosion +
            ", depth=" + depth +
            ", weirdness=" + weirdness +
            ", radius=" + radius +
            '}';
    }

    public Injection remap(UnaryOperator<Climate.TargetPoint> operator) {
        float[] center = new float[behaviours.length];
        for (int i = 0; i < behaviours.length; i++) {
            var behaviour = behaviours[i];
            center[i] = behaviour.center();
        }
        Climate.TargetPoint centerClimate = climateOf(center);
        Climate.TargetPoint remappedCenterClimate = operator.apply(centerClimate);
        DimensionBehaviour temperature;
        DimensionBehaviour humidity;
        DimensionBehaviour.Range continentalness = this.continentalness;
        DimensionBehaviour erosion;
        DimensionBehaviour.Range depth = this.depth;
        DimensionBehaviour weirdness;
        if (this.temperature.asSquish() != null) {
            temperature = new DimensionBehaviour.Squish(unquantizeAndClamp(remappedCenterClimate.temperature()));
        } else {
            float centerTemperature = center[0];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.temperature.asRange());
            center[0] = range.max();
            Climate.TargetPoint maxTemperatureClimate = climateOf(center);
            long maxTemperature = maxTemperatureClimate.temperature();
            center[0] = range.min();
            Climate.TargetPoint minTemperatureClimate = climateOf(center);
            long minTemperature = minTemperatureClimate.temperature();
            center[0] = centerTemperature;
            temperature = new DimensionBehaviour.Range(
                unquantizeAndClamp(minTemperature),
                unquantizeAndClamp(maxTemperature)
            );
        }
        if (this.humidity.asSquish() != null) {
            humidity = new DimensionBehaviour.Squish(unquantizeAndClamp(remappedCenterClimate.humidity()));
        } else {
            float centerHumidity = center[1];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.humidity.asRange());
            center[1] = range.max();
            Climate.TargetPoint maxHumidityClimate = climateOf(center);
            long maxHumidity = maxHumidityClimate.humidity();
            center[1] = range.min();
            Climate.TargetPoint minHumidityClimate = climateOf(center);
            long minHumidity = minHumidityClimate.humidity();
            center[1] = centerHumidity;
            humidity = new DimensionBehaviour.Range(
                unquantizeAndClamp(minHumidity),
                unquantizeAndClamp(maxHumidity)
            );
        }
        if (this.erosion.asSquish() != null) {
            erosion = new DimensionBehaviour.Squish(unquantizeAndClamp(remappedCenterClimate.erosion()));
        } else {
            float centerErosion = center[3];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.erosion.asRange());
            center[3] = range.max();
            Climate.TargetPoint maxErosionClimate = climateOf(center);
            long maxErosion = maxErosionClimate.erosion();
            center[3] = range.min();
            Climate.TargetPoint minErosionClimate = climateOf(center);
            long minErosion = minErosionClimate.erosion();
            center[3] = centerErosion;
            erosion = new DimensionBehaviour.Range(
                unquantizeAndClamp(minErosion),
                unquantizeAndClamp(maxErosion)
            );
        }
        if (this.weirdness.asSquish() != null) {
            weirdness = new DimensionBehaviour.Squish(unquantizeAndClamp(remappedCenterClimate.weirdness()));
        } else {
            float centerWeirdness = center[5];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.weirdness.asRange());
            center[5] = range.max();
            Climate.TargetPoint maxWeirdnessClimate = climateOf(center);
            long maxWeirdness = maxWeirdnessClimate.weirdness();
            center[5] = range.min();
            Climate.TargetPoint minWeirdnessClimate = climateOf(center);
            long minWeirdness = minWeirdnessClimate.weirdness();
            center[5] = centerWeirdness;
            weirdness = new DimensionBehaviour.Range(
                unquantizeAndClamp(minWeirdness),
                unquantizeAndClamp(maxWeirdness)
            );
        }
        return new Injection(
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness,
            radius
        );
    }

    public Climate.TargetPoint center(Climate.TargetPoint initial) {
        return new Climate.TargetPoint(
            temperature.asSquish() != null ? Climate.quantizeCoord(temperature.center()) : initial.temperature(),
            humidity.asSquish() != null ? Climate.quantizeCoord(humidity.center()) : initial.humidity(),
            initial.continentalness(),
            erosion.asSquish() != null ? Climate.quantizeCoord(erosion.center()) : initial.erosion(),
            initial.depth(),
            weirdness.asSquish() != null ? Climate.quantizeCoord(weirdness.center()) : initial.weirdness()
        );
    }
}
