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

    public @NotNull Climate.TargetPoint unsquish(Climate.TargetPoint initial, Relative.Series relatives) {
        float[] thePoint = new float[] {
            Climate.unquantizeCoord(initial.temperature()),
            Climate.unquantizeCoord(initial.humidity()),
            Climate.unquantizeCoord(initial.continentalness()),
            Climate.unquantizeCoord(initial.erosion()),
            Climate.unquantizeCoord(initial.depth()),
            Climate.unquantizeCoord(initial.weirdness())
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

        float distSquare = Float.MAX_VALUE;
        for (int i = 0; i < squishCount; i++) {
            float diff = diffs[i];
            if (diff < 0) {
                float time = (1 + toUnsquish[i]) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = -1;
                    } else {
                        scaled[j] = toUnsquish[j] + diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toUnsquish, scaled));
            } else if (diff > 0) {
                float time = (1 - toUnsquish[i]) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = 1;
                    } else {
                        scaled[j] = toUnsquish[j] + diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toUnsquish, scaled));
            }
        }

        float relativeDistance;

        if (distSquare == Float.MAX_VALUE) {
            relativeDistance = 0;
        } else {
            float multiplier = 1;

            for (int i = 0; i < rangeCount; i++) {
                float p = thePoint[rangeIndices[i]];
                DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
                if (p < range.min()) {
                    multiplier *= (p + 1) / (range.min() + 1);
                } else if (p > range.max()) {
                    multiplier *= (1 - p) / (1 - range.max());
                }
            }

            float distanceToEdge = Mth.sqrt(distSquare);
            float distanceToCenter = Mth.sqrt(distanceSquare(toUnsquish, toUnsquishCenters));
            relativeDistance = distanceToCenter / (distanceToEdge + distanceToCenter);
            relativeDistance = 1 - (1 - relativeDistance) / multiplier / multiplier;
        }

        double relativeVolume = Math.pow(radius, squishCount);
        float finalDist = (float) Math.pow((1 - relativeVolume)*Math.pow(relativeDistance, squishCount) + relativeVolume, 1.0 / squishCount);

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
            Climate.unquantizeCoord(initial.temperature()),
            Climate.unquantizeCoord(initial.humidity()),
            Climate.unquantizeCoord(initial.continentalness()),
            Climate.unquantizeCoord(initial.erosion()),
            Climate.unquantizeCoord(initial.depth()),
            Climate.unquantizeCoord(initial.weirdness())
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
                float time = (1 + toSquish[i]) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = -1;
                    } else {
                        scaled[j] = toSquish[j] + diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toSquish, scaled));
            } else if (diff > 0) {
                float time = (1 - toSquish[i]) / diff;
                float[] scaled = new float[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = 1;
                    } else {
                        scaled[j] = toSquish[j] + diffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(toSquish, scaled));
            }
        }

        float relativeDistance;

        if (distSquare == Float.MAX_VALUE) {
            relativeDistance = 0;
        } else {
            float multiplier = 1;

            for (int i = 0; i < rangeCount; i++) {
                float p = thePoint[rangeIndices[i]];
                DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
                if (p < range.min()) {
                    multiplier *= (p + 1) / (range.min() + 1);
                } else if (p > range.max()) {
                    multiplier *= (1 - p) / (1 - range.max());
                }
            }

            float distanceToEdge = Mth.sqrt(distSquare);
            float distanceToCenter = Mth.sqrt(distanceSquare(toSquish, toSquishCenters));
            relativeDistance = distanceToCenter / (distanceToEdge + distanceToCenter);
            relativeDistance = 1 - (1 - relativeDistance) * multiplier * multiplier;
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
            float[] out = new float[behaviours.length];
            for (int i = 0; i < behaviours.length; i++) {
                var behaviour = behaviours[i];
                if (behaviour.asSquish() != null) {
                    out[i] = behaviour.asSquish().position();
                } else {
                    out[i] = thePoint[i];
                }
            }
            return climateOf(out);
        }

        double relativeVolume = Math.pow(radius, squishCount);
        float movedDistRatio = (float) (relativeDistance - Math.pow((Math.pow(relativeDistance, squishCount) - relativeVolume)/(1 - relativeVolume), 1.0 / squishCount)) / relativeDistance;

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

    float relativeVolume() {
        float relativeVolume = 1;
        for (int i = 0; i < squishCount; i++) {
            relativeVolume *= radius;
        }
        for (int i = 0; i < rangeCount; i++) {
            DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
            relativeVolume *= (range.max() - range.min()) / 2;
        }
        return relativeVolume;
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
            temperature = new DimensionBehaviour.Squish(Climate.unquantizeCoord(remappedCenterClimate.temperature()));
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
                Climate.unquantizeCoord(minTemperature),
                Climate.unquantizeCoord(maxTemperature)
            );
        }
        if (this.humidity.asSquish() != null) {
            humidity = new DimensionBehaviour.Squish(Climate.unquantizeCoord(remappedCenterClimate.humidity()));
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
                Climate.unquantizeCoord(minHumidity),
                Climate.unquantizeCoord(maxHumidity)
            );
        }
        if (this.erosion.asSquish() != null) {
            erosion = new DimensionBehaviour.Squish(Climate.unquantizeCoord(remappedCenterClimate.erosion()));
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
                Climate.unquantizeCoord(minErosion),
                Climate.unquantizeCoord(maxErosion)
            );
        }
        if (this.weirdness.asSquish() != null) {
            weirdness = new DimensionBehaviour.Squish(Climate.unquantizeCoord(remappedCenterClimate.weirdness()));
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
                Climate.unquantizeCoord(minWeirdness),
                Climate.unquantizeCoord(maxWeirdness)
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
}
