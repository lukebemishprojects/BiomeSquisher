package dev.lukebemish.biomesquisher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Injection {
    private final DimensionBehaviour temperature;
    private final DimensionBehaviour humidity;
    private final DimensionBehaviour.Range continentalness;
    private final DimensionBehaviour erosion;
    private final DimensionBehaviour.Range depth;
    private final DimensionBehaviour weirdness;
    private final double radius;

    // derived
    private final DimensionBehaviour[] behaviours;
    private final int squishCount;
    private final int[] squishIndices;
    private final int rangeCount;
    private final int[] rangeIndices;
    private final double degreeScaling;

    public static final Codec<Injection> CODEC = RecordCodecBuilder.<Injection>mapCodec(i -> i.group(
        DimensionBehaviour.CODEC.fieldOf("temperature").forGetter(Injection::temperature),
        DimensionBehaviour.CODEC.fieldOf("humidity").forGetter(Injection::humidity),
        DimensionBehaviour.CODEC.comapFlatMap(b -> (b instanceof DimensionBehaviour.Range range) ? DataResult.success(range) : DataResult.error(() -> "Continentalness must be a range"), Function.identity()).fieldOf("continentalness").forGetter(Injection::continentalness),
        DimensionBehaviour.CODEC.fieldOf("erosion").forGetter(Injection::erosion),
        DimensionBehaviour.CODEC.comapFlatMap(b -> (b instanceof DimensionBehaviour.Range range) ? DataResult.success(range) : DataResult.error(() -> "Depth must be a range"), Function.identity()).fieldOf("depth").forGetter(Injection::depth),
        DimensionBehaviour.CODEC.fieldOf("weirdness").forGetter(Injection::weirdness),
        Codec.DOUBLE.fieldOf("radius").forGetter(Injection::radius)
    ).apply(i, Injection::new)).flatXmap(Injection::verify, DataResult::success).codec();

    private DataResult<Injection> verify() {
        if (squishCount < 2) {
            return DataResult.error(() -> "Must have at least 2 squish dimensions");
        }
        return DataResult.success(this);
    }

    public static Injection of(
        DimensionBehaviour temperature,
        DimensionBehaviour humidity,
        DimensionBehaviour.Range continentalness,
        DimensionBehaviour erosion,
        DimensionBehaviour.Range depth,
        DimensionBehaviour weirdness,
        double radius
    ) {
        var result = new Injection(
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness,
            radius
        ).verify();
        if (result.error().isPresent()) {
            throw new IllegalArgumentException(result.error().get().message());
        }
        //noinspection OptionalGetWithoutIsPresent
        return result.result().get();
    }

    private Injection(
        DimensionBehaviour temperature,
        DimensionBehaviour humidity,
        DimensionBehaviour.Range continentalness,
        DimensionBehaviour erosion,
        DimensionBehaviour.Range depth,
        DimensionBehaviour weirdness,
        double radius
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
        if (squishCount != 0) {
            double totalDegree = 1;
            for (int i = 0; i < squishCount; i++) {
                //noinspection DataFlowIssue
                totalDegree *= behaviours[squishIndices[i]].asSquish().degree();
            }
            this.degreeScaling = Math.pow(totalDegree, 1.0 / squishCount);
        } else {
            this.degreeScaling = 1;
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

    public double radius() {
        return radius;
    }

    public static double unquantizeAndClamp(long coord) {
        return Mth.clamp(Climate.unquantizeCoord(coord), -1, 1);
    }
    private double[] findRelativePosition(double[] centers, double[] point) {
        double[] relative = new double[squishCount];
        for (int i = 0; i < squishCount; i++) {
            double diff = centers[i] - point[i];
            //noinspection DataFlowIssue
            double power = behaviours[squishIndices[i]].asSquish().degree() / this.degreeScaling;
            if (diff < 0) {
                relative[i] = Math.pow(diff / (centers[i] - 1), power);
            } else if (diff > 0) {
                relative[i] = -Math.pow(diff / (centers[i] + 1), power);
            }
        }
        return relative;
    }

    private double findAbsolutePosition(double center, double relative, int i) {
        //noinspection DataFlowIssue
        double power = behaviours[squishIndices[i]].asSquish().degree() / this.degreeScaling;
        if (relative < 0) {
            return center - Math.pow(-relative, 1d / power) * (center + 1);
        } else if (relative > 0) {
            return center + Math.pow(relative, 1d / power) * (1 - center);
        } else {
            return center;
        }
    }

    public double @NotNull [] unsquish(double[] original, Relative.Series relatives) {
        double[] thePoint = Arrays.copyOf(original, original.length);
        double[] relativeEdge = new double[squishCount];
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

        SquishingResolt result = squishingResult(thePoint);

        double multiplier = calculateMultiplier(thePoint);

        if (multiplier == 0) {
            return thePoint;
        }

        double smallRadius = radius * (1 - multiplier);

        if (result.relativeDistance() < smallRadius && result.relativeDistance() != 0) {
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                double diff = (result.squishCenter()[i] - thePoint[squishIndices[i]]) / (1 - multiplier);
                thePoint[squishIndices[i]] += diff;
            }

            return thePoint;
        }

        double relativeVolume = Math.pow(radius, squishCount);
        double relativeSmallVolume = Math.pow(smallRadius, squishCount);
        double finalDist = Math.pow(
            (1 - relativeVolume) * (Math.pow(result.relativeDistance(), squishCount) - relativeSmallVolume) / (1 - relativeSmallVolume) + relativeVolume,
            1.0 / squishCount
        );

        if (result.relativeDistance() == 0) {
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                double diff = relativeEdge[i] * finalDist;
                thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter()[i], diff, i);
            }

            return thePoint;
        }

        double finalRatio = finalDist / result.relativeDistance();

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            double diff = result.relativeDiffs()[i] * finalRatio;
            thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
        }

        return thePoint;
    }

    @NotNull
    private SquishingResolt squishingResult(double[] thePoint) {
        double[] squishPoint = new double[squishCount];
        double[] squishCenter = new double[squishCount];
        for (int i = 0; i < squishCount; i++) {
            double o = thePoint[squishIndices[i]];
            squishPoint[i] = o;
            //noinspection DataFlowIssue
            double c = behaviours[squishIndices[i]].asSquish().position();
            squishCenter[i] = c;
        }
        double[] relativeDiffs = findRelativePosition(squishCenter, squishPoint);

        double distSquare = Double.MAX_VALUE;
        for (int i = 0; i < squishCount; i++) {
            double diff = relativeDiffs[i];
            if (diff < 0) {
                double time = -1d / diff;
                double[] scaled = new double[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = -1;
                    } else {
                        scaled[j] = relativeDiffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(scaled));
            } else if (diff > 0) {
                double time = 1 / diff;
                double[] scaled = new double[squishCount];
                for (int j = 0; j < squishCount; j++) {
                    if (j == i) {
                        scaled[j] = 1;
                    } else {
                        scaled[j] = relativeDiffs[j] * time;
                    }
                }
                distSquare = Math.min(distSquare, distanceSquare(scaled));
            }
        }

        double relativeDistance;
        double distanceToCenter = Math.sqrt(distanceSquare(relativeDiffs));

        if (distSquare == Double.MAX_VALUE) {
            relativeDistance = 0;
        } else {
            double distanceTotal = Math.sqrt(distSquare);
            relativeDistance = distanceToCenter / distanceTotal;
        }
        return new SquishingResolt(squishCenter, relativeDiffs, relativeDistance);
    }

    private record SquishingResolt(double[] squishCenter, double[] relativeDiffs, double relativeDistance) {}

    private double calculateMultiplier(double[] thePoint) {
        double multiplier = 1;

        for (int i = 0; i < rangeCount; i++) {
            double p = thePoint[rangeIndices[i]];
            DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
            if (p < range.min()) {
                double total = range.min() + 1;
                double partial = range.min() - p;
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            } else if (p > range.max()) {
                double total = 1 - range.max();
                double partial = p - range.max();
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            }
        }
        return multiplier;
    }

    public @Nullable Climate.TargetPoint squish(Climate.TargetPoint initial) {
        double[] thePoint = new double[] {
            unquantizeAndClamp(initial.temperature()),
            unquantizeAndClamp(initial.humidity()),
            unquantizeAndClamp(initial.continentalness()),
            unquantizeAndClamp(initial.erosion()),
            unquantizeAndClamp(initial.depth()),
            unquantizeAndClamp(initial.weirdness())
        };

        SquishingResolt result = squishingResult(thePoint);

        if (result.relativeDistance <= radius) {
            boolean isInRange = true;
            for (int i = 0; i < rangeCount; i++) {
                @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(behaviours[rangeIndices[i]].asRange());
                double min = range.min();
                double max = range.max();
                double value = thePoint[rangeIndices[i]];
                if (value < min || value > max) {
                    isInRange = false;
                }
            }
            if (isInRange) {
                return null;
            }
        }

        double multiplier = calculateMultiplier(thePoint);

        if (result.relativeDistance <= radius) {
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                double diff = (result.squishCenter()[i] - thePoint[squishIndices[i]]) * multiplier;
                thePoint[squishIndices[i]] += diff;
            }
            return climateOf(thePoint);
        }

        double smallRadius = radius * (1 - multiplier);
        double relativeVolume = Math.pow(radius, squishCount);
        double relativeSmallVolume = Math.pow(smallRadius, squishCount);
        double finalDist = Math.pow(
            (1 - relativeSmallVolume) * (Math.pow(result.relativeDistance, squishCount) - relativeVolume) / (1 - relativeVolume) + relativeSmallVolume,
            1.0 / squishCount
        );
        double finalRatio = finalDist / result.relativeDistance;

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            double diff = result.relativeDiffs[i] * finalRatio;
            thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
        }

        return climateOf(thePoint);
    }

    private static Climate.TargetPoint climateOf(double[] out) {
        return new Climate.TargetPoint(
            quantizeCoord(out[0]),
            quantizeCoord(out[1]),
            quantizeCoord(out[2]),
            quantizeCoord(out[3]),
            quantizeCoord(out[4]),
            quantizeCoord(out[5])
        );
    }

    public static long quantizeCoord(double v) {
        return (long) (v * 10000.0);
    }

    private static double distanceSquare(double[] b) {
        double sum = 0;
        for (double diff : b) {
            sum += diff * diff;
        }
        return sum;
    }

    Injection scale(double totalVolume) {
        return new Injection(
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness,
            radius / Math.pow(totalVolume, 1.0 / squishCount));
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

    public Injection remap(UnaryOperator<double[]> operator) {
        double[] center = new double[behaviours.length];
        for (int i = 0; i < behaviours.length; i++) {
            var behaviour = behaviours[i];
            center[i] = behaviour.center();
        }
        double[] remappedCenter = operator.apply(center);
        DimensionBehaviour temperature;
        DimensionBehaviour humidity;
        DimensionBehaviour.Range continentalness = this.continentalness;
        DimensionBehaviour erosion;
        DimensionBehaviour.Range depth = this.depth;
        DimensionBehaviour weirdness;
        if (this.temperature.asSquish() != null) {
            temperature = new DimensionBehaviour.Squish(remappedCenter[0], this.temperature.asSquish().degree());
        } else {
            double centerTemperature = center[0];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.temperature.asRange());
            center[0] = range.max();
            double maxTemperature = operator.apply(center)[0];
            center[0] = range.min();
            double minTemperature = operator.apply(center)[0];
            center[0] = centerTemperature;
            temperature = new DimensionBehaviour.Range(
                minTemperature,
                maxTemperature
            );
        }
        if (this.humidity.asSquish() != null) {
            humidity = new DimensionBehaviour.Squish(remappedCenter[1], this.humidity.asSquish().degree());
        } else {
            double centerHumidity = center[1];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.humidity.asRange());
            center[1] = range.max();
            double maxHumidity = operator.apply(center)[1];
            center[1] = range.min();
            double minHumidity = operator.apply(center)[1];
            center[1] = centerHumidity;
            humidity = new DimensionBehaviour.Range(
                minHumidity,
                maxHumidity
            );
        }
        if (this.erosion.asSquish() != null) {
            erosion = new DimensionBehaviour.Squish(remappedCenter[3], this.erosion.asSquish().degree());
        } else {
            double centerErosion = center[3];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.erosion.asRange());
            center[3] = range.max();
            double maxErosion = operator.apply(center)[3];
            center[3] = range.min();
            double minErosion = operator.apply(center)[3];
            center[3] = centerErosion;
            erosion = new DimensionBehaviour.Range(
                minErosion,
                maxErosion
            );
        }
        if (this.weirdness.asSquish() != null) {
            weirdness = new DimensionBehaviour.Squish(remappedCenter[5], this.weirdness.asSquish().degree());
        } else {
            double centerWeirdness = center[5];
            @NotNull DimensionBehaviour.Range range = Objects.requireNonNull(this.weirdness.asRange());
            center[5] = range.max();
            double maxWeirdness = operator.apply(center)[5];
            center[5] = range.min();
            double minWeirdness = operator.apply(center)[5];
            center[5] = centerWeirdness;
            weirdness = new DimensionBehaviour.Range(
                minWeirdness,
                maxWeirdness
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
