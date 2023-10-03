package dev.lukebemish.biomesquisher;

import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class BiomeSquisher {
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

    public BiomeSquisher(
            float temperatureCenter, float temperatureSpread,
            float humidityCenter, float humiditySpread,
            float continentalnessCenter, float continentalnessSpread,
            float erosionCenter, float erosionSpread,
            // Ignore depth, to not squish oceans badly
            float weirdnessCenter, float weirdnessSpread
    ) {
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
        this.spreadSquareMagnitude = temperatureSpread * temperatureSpread
                + humiditySpread * humiditySpread
                + continentalnessSpread * continentalnessSpread
                + erosionSpread * erosionSpread
                + weirdnessSpread * weirdnessSpread;
    }

    public @Nullable Climate.TargetPoint squish(Climate.TargetPoint initial) {
        float temperature = Climate.unquantizeCoord(initial.temperature());
        float humidity = Climate.unquantizeCoord(initial.humidity());
        float continentalness = Climate.unquantizeCoord(initial.continentalness());
        float erosion = Climate.unquantizeCoord(initial.erosion());
        float weirdness = Climate.unquantizeCoord(initial.weirdness());

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
            return null;
        }

        float movedDistRatio = (dist - Mth.sqrt(dist * dist - 1)/(this.spreadSquareMagnitude-1));
        float tDiff = temperatureCenter - temperature;
        float hDiff = humidityCenter - humidity;
        float cDiff = continentalnessCenter - continentalness;
        float eDiff = erosionCenter - erosion;
        float wDiff = weirdnessCenter - weirdness;

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
        float distance = (0.5f - Math.abs(temperature - 0.5f)) / ts;
        float hDistance;
        float cDistance;
        float eDistance;
        float wDistance;
        if ((hDistance = (0.5f - Math.abs(humidity - 0.5f)) / hs) < distance) {
            distance = hDistance;
        }
        if ((cDistance = (0.5f - Math.abs(continentalness - 0.5f)) / cs) < distance) {
            distance = cDistance;
        }
        if ((eDistance = (0.5f - Math.abs(erosion - 0.5f)) / es) < distance) {
            distance = eDistance;
        }
        if ((wDistance = (0.5f - Math.abs(weirdness - 0.5f)) / ws) < distance) {
            distance = wDistance;
        }
        return distance;
    }

    private static float distanceToEdgeProjection(
            float tCenter, float hCenter, float cCenter, float eCenter, float wCenter,
            float ts, float hs, float cs, float es, float ws,
            float tOriginal, float hOriginal, float cOriginal, float eOriginal, float wOriginal
    ) {
        float tDiff = tCenter - tOriginal;
        float hDiff = hCenter - hOriginal;
        float cDiff = cCenter - cOriginal;
        float eDiff = eCenter - eOriginal;
        float wDiff = wCenter - wOriginal;
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
                    0, h, c, e, w));
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
                    t, 0, c, e, w));
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
                    t, h, 0, e, w));
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
                    t, h, c, 0, w));
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
                    t, h, c, e, 0));
        }

        if (distance > 6) {
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
}
