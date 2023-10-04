package dev.lukebemish.biomesquisher;

import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

public final class TargetTransformer {
    private final float temperatureCenter;
    private final float temperatureSpread;
    private final float humidityCenter;
    private final float humiditySpread;
    private final float continentalnessCenter;
    private final float continentalnessSpread;
    private final float erosionCenter;
    private final float erosionSpread;
    private final float weirdnessCenter;
    private final float weirdnessSpread;
    private final float spreadSquareMagnitude;
    private final float volume;
    private final float depthStart;
    private final float depthEnd;

    public TargetTransformer(
        float temperatureCenter, float temperatureSpread,
        float humidityCenter, float humiditySpread,
        float continentalnessCenter, float continentalnessSpread,
        float erosionCenter, float erosionSpread,
        // Ignore depth, to not squish oceans badly
        float weirdnessCenter, float weirdnessSpread,
        // And have a fixed range for depth to squish less outside of that
        float depthStart, float depthEnd) {
        this.temperatureCenter = temperatureCenter;
        this.temperatureSpread = temperatureSpread;
        this.humidityCenter = humidityCenter;
        this.humiditySpread = humiditySpread;
        this.continentalnessCenter = continentalnessCenter;
        this.continentalnessSpread = continentalnessSpread;
        this.erosionCenter = erosionCenter;
        this.erosionSpread = erosionSpread;
        this.weirdnessCenter = weirdnessCenter;
        this.weirdnessSpread = weirdnessSpread;
        this.depthStart = depthStart;
        this.depthEnd = depthEnd;
        this.spreadSquareMagnitude = temperatureSpread * temperatureSpread
                + humiditySpread * humiditySpread
                + continentalnessSpread * continentalnessSpread
                + erosionSpread * erosionSpread
                + weirdnessSpread * weirdnessSpread;
        this.volume = temperatureSpread * humiditySpread * continentalnessSpread * erosionSpread * weirdnessSpread;
    }

    TargetTransformer scale(float oldVolume) {
        return new TargetTransformer(
                temperatureCenter, temperatureSpread / oldVolume,
                humidityCenter, humiditySpread / oldVolume,
                continentalnessCenter, continentalnessSpread / oldVolume,
                erosionCenter, erosionSpread / oldVolume,
                weirdnessCenter, weirdnessSpread / oldVolume,
            depthStart, depthEnd);
    }

    public @Nullable Climate.TargetPoint squish(Climate.TargetPoint initial) {
        float temperature = Climate.unquantizeCoord(initial.temperature());
        float humidity = Climate.unquantizeCoord(initial.humidity());
        float continentalness = Climate.unquantizeCoord(initial.continentalness());
        float erosion = Climate.unquantizeCoord(initial.erosion());
        float weirdness = Climate.unquantizeCoord(initial.weirdness());
        float depth = Climate.unquantizeCoord(initial.depth());

        float distanceToEdgeProjected = distanceToEdgeProjection(
                temperatureCenter, humidityCenter, continentalnessCenter, erosionCenter, weirdnessCenter,
                temperatureSpread, humiditySpread, continentalnessSpread, erosionSpread, weirdnessSpread,
                temperature, humidity, continentalness, erosion, weirdness
        );
        float pureDistance = Mth.sqrt(distanceSquare(
                temperatureCenter, humidityCenter, continentalnessCenter, erosionCenter, weirdnessCenter,
                temperatureSpread, humiditySpread, continentalnessSpread, erosionSpread, weirdnessSpread,
                temperature, humidity, continentalness, erosion, weirdness
        ));

        float dist = pureDistance / (distanceToEdgeProjected + pureDistance);

        if (dist < 1) {
            if (depth >= depthStart && depth <= depthEnd) {
                return null;
            }
            return initial;
        }

        float movedDistRatio = (dist - Mth.sqrt(dist * dist - 1)/(this.spreadSquareMagnitude-1));
        float tDiff = temperatureCenter - temperature;
        float hDiff = humidityCenter - humidity;
        float cDiff = continentalnessCenter - continentalness;
        float eDiff = erosionCenter - erosion;
        float wDiff = weirdnessCenter - weirdness;

        if (depth < depthStart) {
            movedDistRatio *= (depth + 1) / (depthStart + 1);
        } else if (depth > depthEnd) {
            movedDistRatio *= (1 - depth) / (1 - depthEnd);
        }

        return new Climate.TargetPoint(
                Climate.quantizeCoord(temperature + tDiff * movedDistRatio),
                Climate.quantizeCoord(humidity + hDiff * movedDistRatio),
                Climate.quantizeCoord(continentalness + cDiff * movedDistRatio),
                Climate.quantizeCoord(erosion + eDiff * movedDistRatio),
                initial.depth(),
                Climate.quantizeCoord(weirdness + wDiff * movedDistRatio)
        );
    }

    private static float distanceToEdge(
            float temperature, float humidity, float continentalness, float erosion, float weirdness,
            float ts, float hs, float cs, float es, float ws
    ) {
        float distance = (1 - Math.abs(temperature)) / ts;
        float hDistance;
        float cDistance;
        float eDistance;
        float wDistance;
        if ((hDistance = (1 - Math.abs(humidity)) / hs) < distance) {
            distance = hDistance;
        }
        if ((cDistance = (1 - Math.abs(continentalness)) / cs) < distance) {
            distance = cDistance;
        }
        if ((eDistance = (1 - Math.abs(erosion)) / es) < distance) {
            distance = eDistance;
        }
        if ((wDistance = (1 - Math.abs(weirdness)) / ws) < distance) {
            distance = wDistance;
        }
        return distance;
    }

    private static float distanceToEdgeProjection(
            float tCenter, float hCenter, float cCenter, float eCenter, float wCenter,
            float ts, float hs, float cs, float es, float ws,
            float tOriginal, float hOriginal, float cOriginal, float eOriginal, float wOriginal
    ) {
        float tDiff = tOriginal - tCenter;
        float hDiff = hOriginal - hCenter;
        float cDiff = cOriginal - cCenter;
        float eDiff = eOriginal - eCenter;
        float wDiff = wOriginal - wCenter;
        float distance = Float.MAX_VALUE;

        if (tDiff > 0) {
            float time = (1 - tOriginal) / tDiff;
            float h = hOriginal + hDiff * time;
            float c = cOriginal + cDiff * time;
            float e = eOriginal + eDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    1, h, c, e, w));
        } else if (tDiff < 0) {
            float time = tOriginal / tDiff;
            float h = hOriginal + hDiff * time;
            float c = cOriginal + cDiff * time;
            float e = eOriginal + eDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    -1, h, c, e, w));
        }

        if (hDiff > 0) {
            float time = (1 - hOriginal) / hDiff;
            float t = tOriginal + tDiff * time;
            float c = cOriginal + cDiff * time;
            float e = eOriginal + eDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, 1, c, e, w));
        } else if (hDiff < 0) {
            float time = hOriginal / hDiff;
            float t = tOriginal + tDiff * time;
            float c = cOriginal + cDiff * time;
            float e = eOriginal + eDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, -1, c, e, w));
        }

        if (cDiff > 0) {
            float time = (1 - cOriginal) / cDiff;
            float t = tOriginal + tDiff * time;
            float h = hOriginal + hDiff * time;
            float e = eOriginal + eDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, h, 1, e, w));
        } else if (cDiff < 0) {
            float time = cOriginal / cDiff;
            float t = tOriginal + tDiff * time;
            float h = hOriginal + hDiff * time;
            float e = eOriginal + eDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, h, -1, e, w));
        }

        if (eDiff > 0) {
            float time = (1 - eOriginal) / eDiff;
            float t = tOriginal + tDiff * time;
            float h = hOriginal + hDiff * time;
            float c = cOriginal + cDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, h, c, 1, w));
        } else if (eDiff < 0) {
            float time = eOriginal / eDiff;
            float t = tOriginal + tDiff * time;
            float h = hOriginal + hDiff * time;
            float c = cOriginal + cDiff * time;
            float w = wOriginal + wDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, h, c, -1, w));
        }

        if (wDiff > 0) {
            float time = (1 - wOriginal) / wDiff;
            float t = tOriginal + tDiff * time;
            float h = hOriginal + hDiff * time;
            float c = cOriginal + cDiff * time;
            float e = eOriginal + eDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, h, c, e, 1));
        } else if (wDiff < 0) {
            float time = wOriginal / wDiff;
            float t = tOriginal + tDiff * time;
            float h = hOriginal + hDiff * time;
            float c = cOriginal + cDiff * time;
            float e = eOriginal + eDiff * time;
            distance = Math.min(distance, distanceSquare(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws,
                    t, h, c, e, -1));
        }

        if (distance > 12) {
            return distanceToEdge(
                    tOriginal, hOriginal, cOriginal, eOriginal, wOriginal,
                    ts, hs, cs, es, ws
            );
        }

        return Mth.sqrt(distance);
    }

    private static float distanceSquare(
            float t1, float h1, float c1, float e1, float w1,
            float ts, float hs, float cs, float es, float ws,
            float t2, float h2, float c2, float e2, float w2
    ) {
        return Mth.square(t1 - t2) / ts + Mth.square(h1 - h2) / hs + Mth.square(c1 - c2) / cs + Mth.square(e1 - e2) / es + Mth.square(w1 - w2) / ws;
    }

    public float volume() {
        return volume;
    }
}
