package dev.lukebemish.biomesquisher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
        if (temperature.isSquish()) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (humidity.isSquish()) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (erosion.isSquish()) {
            totalSquish += 1;
        } else {
            totalRange += 1;
        }
        if (weirdness.isSquish()) {
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
            if (behaviours[i].isSquish()) {
                squishIndices[j] = i;
                j += 1;
            }
        }
        rangeCount = totalRange;
        rangeIndices = new int[rangeCount];
        for (int i = 0, j = 0; i < behaviours.length; i++) {
            if (behaviours[i].isRange()) {
                rangeIndices[j] = i;
                j += 1;
            }
        }
        if (squishCount != 0) {
            double totalDegree = 1;
            for (int i = 0; i < squishCount; i++) {
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

    /**
     * Performs the reverse transformation, going from a coordinate in squished space to a coordinate in unsquished space.
     * In other words, this transformation "eats" a hole ans squishes around the surrounding edge.
     * @param original the original coordinate, in unquantized space (from -1 ro 1)
     * @param relatives the relative series used to determine where the single value where the hole would be should map to
     * @return the coordinate in unsquished space
     */
    public double[] unsquish(double[] original, Relative.Series relatives) {
        double[] thePoint = Arrays.copyOf(original, original.length);
        double[] relativeEdge = new double[squishCount];

        int temperature = -1;
        int humidity = -1;
        int erosion = -1;
        int weirdness = -1;
        for (int i = 0, j = 0; i < behaviours.length; i++) {
            if (behaviours[i].isSquish()) {
                switch (i) {
                    case 0 -> temperature = j;
                    case 1 -> humidity = j;
                    case 3 -> erosion = j;
                    case 5 -> weirdness = j;
                }
                j++;
            }
        }

        // First, handle relatives - this basically just solves for the first valid relative in the series and uses that.
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

        // calculate the relative distances of the point to the target point of the hole
        RelativeDistanceResult result = relativeDistance(thePoint);

        // a multiplier based on distance from the desired range in non-squished dimensions
        double multiplier = calculateMultiplier(thePoint);

        // The point is far enough away from the range that no squishing occurs
        if (multiplier == 0) {
            return thePoint;
        }

        // target radius of "closed" hold
        double smallRadius = radius * (1 - multiplier);

        // We're inside where the (closed) hole would be, but not in the exact center point that doesn't map anywhere.
        // In other words, we're outside of the range in unsquished dimensions - because the target "closed" hold is
        // a point inside the range!
        if (result.relativeDistance() < smallRadius && result.relativeDistance() != 0) {
            // thePoint mutated below here

            // Re-adjust distance based on the multiplier
            for (int i = 0; i < squishCount; i++) {
                double diff = result.relativeDiffs[i] / (1 - multiplier);
                thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
            }

            return thePoint;
        }

        // Calculate relative volumes of the "open" and "closed" holes
        double relativeVolume = Math.pow(radius, squishCount);
        double relativeSmallVolume = Math.pow(smallRadius, squishCount);
        // Calculate the target (relative) distance for this point from the center
        double finalDist = Math.pow(
            (1 - relativeVolume) * (Math.pow(result.relativeDistance(), squishCount) - relativeSmallVolume) / (1 - relativeSmallVolume) + relativeVolume,
            1.0 / squishCount
        );

        if (result.relativeDistance() == 0) {
            // thePoint mutated below here

            for (int i = 0; i < squishCount; i++) {
                // We're inside the "closed" hole and within the range. Use the relative to find target position.
                double diff = relativeEdge[i] * finalDist;
                thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter()[i], diff, i);
            }

            return thePoint;
        }

        double finalRatio = finalDist / result.relativeDistance();

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            // We're outside the "closed" hold.
            double diff = result.relativeDiffs()[i] * finalRatio;
            thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
        }

        return thePoint;
    }

    /**
     * Performs the forward transformation, going from a coordinate in unsquished space to a coordinate in squished space.
     * This creates a "hole" in input space where nothing maps from
     * @param initial the initial coordinate
     * @return the coordinate in squished space, or null if the input coordinate is in the hole
     */
    public @Nullable Climate.TargetPoint squish(Climate.TargetPoint initial) {
        double[] thePoint = new double[] {
            Utils.unquantizeAndClamp(initial.temperature()),
            Utils.unquantizeAndClamp(initial.humidity()),
            Utils.unquantizeAndClamp(initial.continentalness()),
            Utils.unquantizeAndClamp(initial.erosion()),
            Utils.unquantizeAndClamp(initial.depth()),
            Utils.unquantizeAndClamp(initial.weirdness())
        };

        // calculate the relative distances of the point to the target point of the hole
        RelativeDistanceResult result = relativeDistance(thePoint);

        // We're inside the radius of the hole
        if (result.relativeDistance <= radius) {
            boolean isInRange = true;
            for (int i = 0; i < rangeCount; i++) {
                DimensionBehaviour.Range range = behaviours[rangeIndices[i]].asRange();
                double min = range.min();
                double max = range.max();
                double value = thePoint[rangeIndices[i]];
                if (value < min || value > max) {
                    isInRange = false;
                    break;
                }
            }
            if (isInRange) {
                // If we're in the range behaviours as well, return null
                return null;
            }
        }

        // Only close the hole completely if we're inside the range - otherwise, it'll close to a smaller hole
        // (meaning the space inside the original hole can map to the space inside the new one)
        double multiplier = calculateMultiplier(thePoint);

        if (result.relativeDistance <= radius) {
            // thePoint mutated below here

            // We're in the hold but not in range - squish towards the center by the proper amount to fill the "closed" hole properly
            for (int i = 0; i < squishCount; i++) {
                double diff = result.relativeDiffs[i] * (1 - multiplier);
                thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
            }
            return climateOf(thePoint);
        }

        double smallRadius = radius * (1 - multiplier);
        double relativeVolume = Math.pow(radius, squishCount);
        double relativeSmallVolume = Math.pow(smallRadius, squishCount);
        // Similar to the calculation in #unsqush, but in reverse - we're going from a full sized hole to some small hole
        // in relative distance.
        double finalDist = Math.pow(
            (1 - relativeSmallVolume) * (Math.pow(result.relativeDistance, squishCount) - relativeVolume) / (1 - relativeVolume) + relativeSmallVolume,
            1.0 / squishCount
        );
        double finalRatio = finalDist / result.relativeDistance;

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            // We're outside the "closed" hold.
            double diff = result.relativeDiffs[i] * finalRatio;
            thePoint[squishIndices[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
        }

        return climateOf(thePoint);
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

    private DataResult<Injection> verify() {
        if (squishCount < 2) {
            return DataResult.error(() -> "Must have at least 2 squish dimensions");
        }
        return DataResult.success(this);
    }

    /**
     * Finds a relative position, measured in relative distance from the center to the edge, after accounting for shuffling
     * the dimensions around a bit. The total dimensionality will add up to the {@link #squishCount} still, but it is shuffled
     * between dimensions based on the {@link DimensionBehaviour.Squish#degree()} of each dimension.
     */
    private double[] findRelativePosition(double[] centers, double[] point) {
        double[] relative = new double[squishCount];
        for (int i = 0; i < squishCount; i++) {
            double diff = centers[i] - point[i];
            double power = behaviours[squishIndices[i]].asSquish().degree() / this.degreeScaling;
            if (diff < 0) {
                relative[i] = Math.pow(diff / (centers[i] - 1), power);
            } else if (diff > 0) {
                relative[i] = -Math.pow(diff / (centers[i] + 1), power);
            }
        }
        return relative;
    }

    /**
     * Reverses {@link #findRelativePosition(double[], double[])} for a single dimension.
     */
    private double findAbsolutePosition(double center, double relative, int i) {
        double power = behaviours[squishIndices[i]].asSquish().degree() / this.degreeScaling;
        if (relative < 0) {
            return center - Math.pow(-relative, 1d / power) * (center + 1);
        } else if (relative > 0) {
            return center + Math.pow(relative, 1d / power) * (1 - center);
        } else {
            return center;
        }
    }

    /**
     * Finds the relative distance of a point to the center of the hole, and returns a collection of that value and other
     * useful information. The relative distance is the distance from the center of the hole to the point, divided by the
     * distance from the center of the hole to the edge going through the point, all within the "relative space" created
     * by {@link #findRelativePosition(double[], double[])}.
     */
    private RelativeDistanceResult relativeDistance(double[] thePoint) {
        double[] squishPoint = new double[squishCount];
        double[] squishCenter = new double[squishCount];
        for (int i = 0; i < squishCount; i++) {
            double o = thePoint[squishIndices[i]];
            squishPoint[i] = o;
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
        return new RelativeDistanceResult(squishCenter, relativeDiffs, relativeDistance);
    }

    private record RelativeDistanceResult(double[] squishCenter, double[] relativeDiffs, double relativeDistance) {}

    /**
     * Calculates a multiplier based on how far from the range a point is in range-based dimensions. Used to transition
     * gracefully form "squishing" to "no squishing" along the range dimensions, without a hard cutoff or squishing in the
     * direction of the range dimensions.
     */
    private double calculateMultiplier(double[] thePoint) {
        double multiplier = 1;

        for (int i = 0; i < rangeCount; i++) {
            double p = thePoint[rangeIndices[i]];
            DimensionBehaviour.Range range = behaviours[rangeIndices[i]].asRange();
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

    private static Climate.TargetPoint climateOf(double[] out) {
        return new Climate.TargetPoint(
            Utils.quantizeCoord(out[0]),
            Utils.quantizeCoord(out[1]),
            Utils.quantizeCoord(out[2]),
            Utils.quantizeCoord(out[3]),
            Utils.quantizeCoord(out[4]),
            Utils.quantizeCoord(out[5])
        );
    }

    private static double distanceSquare(double[] b) {
        double sum = 0;
        for (double diff : b) {
            sum += diff * diff;
        }
        return sum;
    }

    /**
     * Shrink the volume of this injection by the proper amount so that, in a system with the given sum of original
     * volumes of injections, this injection will be the same size as same-radius injections injected earlier.
     */
    @ApiStatus.Internal
    public Injection scale(double totalVolume) {
        return new Injection(
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness,
            radius / Math.pow(totalVolume, 1.0 / squishCount));
    }

    /**
     * Remap the center (and range bounds) of this injection using the given operator - used with {@link #unsquish(double[], Relative.Series)}
     * to layer injections sensibly.
     */
    @ApiStatus.Internal
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
        if (this.temperature.isSquish()) {
            temperature = new DimensionBehaviour.Squish(remappedCenter[0], this.temperature.asSquish().degree());
        } else {
            double centerTemperature = center[0];
            DimensionBehaviour.Range range = this.temperature.asRange();
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
        if (this.humidity.isSquish()) {
            humidity = new DimensionBehaviour.Squish(remappedCenter[1], this.humidity.asSquish().degree());
        } else {
            double centerHumidity = center[1];
            DimensionBehaviour.Range range = this.humidity.asRange();
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
        if (this.erosion.isSquish()) {
            erosion = new DimensionBehaviour.Squish(remappedCenter[3], this.erosion.asSquish().degree());
        } else {
            double centerErosion = center[3];
            DimensionBehaviour.Range range = this.erosion.asRange();
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
        if (this.weirdness.isSquish()) {
            weirdness = new DimensionBehaviour.Squish(remappedCenter[5], this.weirdness.asSquish().degree());
        } else {
            double centerWeirdness = center[5];
            DimensionBehaviour.Range range = this.weirdness.asRange();
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
