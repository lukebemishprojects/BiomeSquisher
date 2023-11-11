package dev.lukebemish.biomesquisher;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.biomesquisher.impl.Context;
import dev.lukebemish.biomesquisher.impl.Dimension;
import dev.lukebemish.biomesquisher.impl.Utils;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;

public final class Injection {
    private final double radius;

    // derived
    private final DimensionBehaviour[] behaviours;
    private final double degreeScaling;

    public static final Codec<Injection> CODEC = RecordCodecBuilder.<Injection>mapCodec(i -> i.group(
        DimensionBehaviour.CODEC.fieldOf("temperature").forGetter(x -> x.behaviours[Dimension.TEMPERATURE.index()]),
        DimensionBehaviour.CODEC.fieldOf("humidity").forGetter(x -> x.behaviours[Dimension.HUMIDITY.index()]),
        DimensionBehaviour.CODEC.fieldOf("continentalness").forGetter(x -> x.behaviours[Dimension.CONTINENTALNESS.index()]),
        DimensionBehaviour.CODEC.fieldOf("erosion").forGetter(x -> x.behaviours[Dimension.EROSION.index()]),
        DimensionBehaviour.CODEC.fieldOf("depth").forGetter(x -> x.behaviours[Dimension.DEPTH.index()]),
        DimensionBehaviour.CODEC.fieldOf("weirdness").forGetter(x -> x.behaviours[Dimension.WEIRDNESS.index()]),
        Codec.DOUBLE.fieldOf("radius").forGetter(Injection::radius)
    ).apply(i, Injection::new)).flatXmap(Injection::verify, DataResult::success).codec();

    public static Injection of(
        DimensionBehaviour temperature,
        DimensionBehaviour humidity,
        DimensionBehaviour continentalness,
        DimensionBehaviour erosion,
        DimensionBehaviour depth,
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
        DimensionBehaviour continentalness,
        DimensionBehaviour erosion,
        DimensionBehaviour depth,
        DimensionBehaviour weirdness,
        double radius
    ) {
        this.radius = radius;
        this.behaviours = new DimensionBehaviour[] {
            temperature,
            humidity,
            continentalness,
            erosion,
            depth,
            weirdness
        };
        double totalDegree = 1;
        for (int i = 0; i < Dimension.SQUISH_INDEXES.length; i++) {
            totalDegree *= behaviours[Dimension.SQUISH_INDEXES[i]].asSquish().degree();
        }
        this.degreeScaling = Math.pow(totalDegree, 1.0 / Dimension.SQUISH_INDEXES.length);
    }

    public DimensionBehaviour[] behaviours() {
        return behaviours;
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
    @ApiStatus.Internal
    public double[] unsquish(Function<double[], double[]> centerRemainder, double[] original, Relative relatives, Context context) {
        double[] thePoint = Arrays.copyOf(original, original.length);
        double[] relativeEdge = new double[Dimension.SQUISH_INDEXES.length];
        int squishCount = Dimension.SQUISH_INDEXES.length;

        for (int i = 0; i < Dimension.SQUISH_INDEXES.length; i++) {
            relativeEdge[i] = relatives.positions().getOrDefault(Dimension.SQUISH[i], Relative.Position.CENTER).offset();
        }

        // calculate the relative distances of the point to the target point of the hole
        RelativeDistanceResult result = relativeDistance(centerRemainder, thePoint, context);

        // a multiplier based on distance from the desired range in non-squished dimensions
        double multiplier = calculateMultiplier(thePoint, context);

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
                thePoint[Dimension.SQUISH_INDEXES[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
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
                thePoint[Dimension.SQUISH_INDEXES[i]] = findAbsolutePosition(result.squishCenter()[i], diff, i);
            }

            return thePoint;
        }

        double finalRatio = finalDist / result.relativeDistance();

        // thePoint mutated below here

        for (int i = 0; i < squishCount; i++) {
            // We're outside the "closed" hold.
            double diff = result.relativeDiffs()[i] * finalRatio;
            thePoint[Dimension.SQUISH_INDEXES[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
        }

        return thePoint;
    }

    /**
     * Performs the forward transformation, going from a coordinate in unsquished space to a coordinate in squished space.
     * This creates a "hole" in input space where nothing maps from
     * @param initial the initial coordinate
     * @return the coordinate in squished space, or null if the input coordinate is in the hole
     */
    @ApiStatus.Internal
    public @Nullable Climate.TargetPoint squish(Function<double[], double[]> centerRemainder, Climate.TargetPoint initial, Context context) {
        double[] thePoint = new double[] {
            Utils.unquantizeAndClamp(initial.temperature(), context, Dimension.TEMPERATURE),
            Utils.unquantizeAndClamp(initial.humidity(), context, Dimension.HUMIDITY),
            Utils.unquantizeAndClamp(initial.continentalness(), context, Dimension.CONTINENTALNESS),
            Utils.unquantizeAndClamp(initial.erosion(), context, Dimension.EROSION),
            Utils.unquantizeAndClamp(initial.depth(), context, Dimension.DEPTH),
            Utils.unquantizeAndClamp(initial.weirdness(), context, Dimension.WEIRDNESS)
        };

        int squishCount = Dimension.SQUISH_INDEXES.length;
        int rangeCount = Dimension.RANGE_INDEXES.length;

        // calculate the relative distances of the point to the target point of the hole
        RelativeDistanceResult result = relativeDistance(centerRemainder, thePoint, context);

        // We're inside the radius of the hole
        if (result.relativeDistance <= radius) {
            boolean isInRange = true;
            for (int i = 0; i < rangeCount; i++) {
                DimensionBehaviour.Range range = behaviours[Dimension.RANGE_INDEXES[i]].asRange();
                double min = range.min(context, Dimension.values()[Dimension.RANGE_INDEXES[i]]);
                double max = range.max(context, Dimension.values()[Dimension.RANGE_INDEXES[i]]);
                double value = thePoint[Dimension.RANGE_INDEXES[i]];
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
        double multiplier = calculateMultiplier(thePoint, context);

        if (result.relativeDistance <= radius) {
            // thePoint mutated below here

            // We're in the hold but not in range - squish towards the center by the proper amount to fill the "closed" hole properly
            for (int i = 0; i < squishCount; i++) {
                double diff = result.relativeDiffs[i] * (1 - multiplier);
                thePoint[Dimension.SQUISH_INDEXES[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
            }
            return climateOf(thePoint, context);
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
            thePoint[Dimension.SQUISH_INDEXES[i]] = findAbsolutePosition(result.squishCenter[i], diff, i);
        }

        return climateOf(thePoint, context);
    }

    @Override
    public String toString() {
        return "Injection{" +
            "behaviours=" + Arrays.toString(behaviours) +
            '}';
    }

    private DataResult<Injection> verify() {
        for (Dimension dimension : Dimension.values()) {
            if (behaviours[dimension.index()].isRange() && dimension.squish()) {
                return DataResult.error(() -> "Injection for dimension " + dimension.getSerializedName() + " must not be a range behaviour.");
            }
            if (behaviours[dimension.index()].isSquish() && !dimension.squish()) {
                return DataResult.error(() -> "Injection for dimension " + dimension.getSerializedName() + " must be a range behaviour.");
            }
        }
        return DataResult.success(this);
    }

    /**
     * Finds a relative position, measured in relative distance from the center to the edge, after accounting for shuffling
     * the dimensions around a bit. The total dimensionality will add up to the number of squishing dimensions still, but it is shuffled
     * between dimensions based on the {@link DimensionBehaviour.Squish#degree()} of each dimension.
     */
    private double[] findRelativePosition(double[] centers, double[] point) {
        double[] relative = new double[Dimension.SQUISH.length];
        for (int i = 0; i < Dimension.SQUISH.length; i++) {
            double diff = centers[i] - point[i];
            double power = behaviours[Dimension.SQUISH_INDEXES[i]].asSquish().degree() / this.degreeScaling;
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
        double power = behaviours[Dimension.SQUISH_INDEXES[i]].asSquish().degree() / this.degreeScaling;
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
    private RelativeDistanceResult relativeDistance(Function<double[], double[]> centerRemainder, double[] thePoint, Context context) {
        double[] fullCenter = new double[behaviours.length];
        for (int i = 0; i < behaviours.length; i++) {
            var behaviour = behaviours[i];
            if (behaviour.isSquish()) {
                fullCenter[i] = behaviour.asSquish().center(context, Dimension.values()[i]);
            } else {
                fullCenter[i] = thePoint[i];
            }
        }
        fullCenter = centerRemainder.apply(fullCenter);

        int squishCount = Dimension.SQUISH_INDEXES.length;
        double[] squishPoint = new double[squishCount];
        double[] squishCenter = new double[squishCount];
        for (int i = 0; i < squishCount; i++) {
            double o = thePoint[Dimension.SQUISH_INDEXES[i]];
            squishPoint[i] = o;
            double c = fullCenter[Dimension.SQUISH_INDEXES[i]];
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
    private double calculateMultiplier(double[] thePoint, Context context) {
        double multiplier = 1;

        for (int i = 0; i < Dimension.RANGE_INDEXES.length; i++) {
            double p = thePoint[Dimension.RANGE_INDEXES[i]];
            DimensionBehaviour.Range range = behaviours[Dimension.RANGE_INDEXES[i]].asRange();
            double min = range.min(context, Dimension.values()[Dimension.RANGE_INDEXES[i]]);
            double max = range.max(context, Dimension.values()[Dimension.RANGE_INDEXES[i]]);
            if (p < min) {
                double total = min + 1;
                double partial = min - p;
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            } else if (p > max) {
                double total = 1 - max;
                double partial = p - min;
                multiplier *= Math.max(0, 1 - (partial) / Math.min(total, radius));
            }
        }
        return multiplier;
    }

    private static Climate.TargetPoint climateOf(double[] out, Context context) {
        return new Climate.TargetPoint(
            Utils.quantizeCoord(out[0], context, Dimension.TEMPERATURE),
            Utils.quantizeCoord(out[1], context, Dimension.HUMIDITY),
            Utils.quantizeCoord(out[2], context, Dimension.CONTINENTALNESS),
            Utils.quantizeCoord(out[3], context, Dimension.EROSION),
            Utils.quantizeCoord(out[4], context, Dimension.DEPTH),
            Utils.quantizeCoord(out[5], context, Dimension.WEIRDNESS)
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
            behaviours[0],
            behaviours[1],
            behaviours[2],
            behaviours[3],
            behaviours[4],
            behaviours[5],
            radius / Math.pow(totalVolume, 1.0 / Dimension.SQUISH_INDEXES.length));
    }
}
