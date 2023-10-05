package dev.lukebemish.biomesquisher;

import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

public final class TargetTransformer {
    private final float temperatureCenter;
    private final float humidityCenter;
    private final float continentalnessCenter;
    private final float erosionCenter;
    private final float weirdnessCenter;
    private final float relativeSpread;
    private final float relativeVolume;
    private final float depthStart;
    private final float depthEnd;

    public TargetTransformer(
        float temperatureCenter,
        float humidityCenter,
        float continentalnessCenter,
        float erosionCenter,
        // Ignore depth, to not squish oceans badly
        float weirdnessCenter,
        // And have a fixed range for depth to squish less outside of that
        float depthStart, float depthEnd,
        float spread) {
        this.temperatureCenter = temperatureCenter;
        this.humidityCenter = humidityCenter;
        this.continentalnessCenter = continentalnessCenter;
        this.erosionCenter = erosionCenter;
        this.weirdnessCenter = weirdnessCenter;
        this.depthStart = depthStart;
        this.depthEnd = depthEnd;
        this.relativeSpread = spread / 2;
        this.relativeVolume = (float) Math.pow(relativeSpread, 5);
    }

    TargetTransformer scale(float oldVolume) {
        return new TargetTransformer(
                temperatureCenter,
                humidityCenter,
                continentalnessCenter,
                erosionCenter,
                weirdnessCenter,
            depthStart, depthEnd,
            (float) (relativeSpread / Math.pow(oldVolume, 0.2)));
    }

    public @Nullable Climate.TargetPoint desquish(Climate.TargetPoint initial) {
        float temperature = Climate.unquantizeCoord(initial.temperature());
        float humidity = Climate.unquantizeCoord(initial.humidity());
        float continentalness = Climate.unquantizeCoord(initial.continentalness());
        float erosion = Climate.unquantizeCoord(initial.erosion());
        float weirdness = Climate.unquantizeCoord(initial.weirdness());
        float depth = Climate.unquantizeCoord(initial.depth());

        // TODO: implement
        return null;
    }

    public @Nullable Climate.TargetPoint squish(Climate.TargetPoint initial) {
        float temperature = Climate.unquantizeCoord(initial.temperature());
        float humidity = Climate.unquantizeCoord(initial.humidity());
        float continentalness = Climate.unquantizeCoord(initial.continentalness());
        float erosion = Climate.unquantizeCoord(initial.erosion());
        float weirdness = Climate.unquantizeCoord(initial.weirdness());
        float depth = Climate.unquantizeCoord(initial.depth());

        float dist = distanceScaledProjection(
            temperature, humidity, continentalness, erosion, weirdness,
            temperatureCenter, humidityCenter, continentalnessCenter, erosionCenter, weirdnessCenter
        );

        if (dist < relativeSpread) {
            if (depth >= depthStart && depth <= depthEnd) {
                return null;
            }
            return initial;
        }

        float movedDistRatio = (float) (dist - Math.pow((Math.pow(dist, 5) - this.relativeVolume)/(1 - this.relativeVolume), 0.2)) / dist;
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

    private float distanceScaledProjection(
        float tO, float hO, float cO, float eO, float wO,
        float tC, float hC, float cC, float eC, float wC) {

        float tDiff = tO - tC;
        float hDiff = hO - hC;
        float cDiff = cO - cC;
        float eDiff = eO - eC;
        float wDiff = wO - wC;

        float distance = Float.MAX_VALUE;

        if (tDiff > 0) {
            float time = (1 - tO) / tDiff;
            float h = hO + hDiff * time;
            float c = cO + cDiff * time;
            float e = eO + eDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                1, h, c, e, w);
            if (dist < distance) {
                distance = dist;
            }
        } else if (tDiff < 0) {
            float time = (1 + tO) / tDiff;
            float h = hO + hDiff * time;
            float c = cO + cDiff * time;
            float e = eO + eDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                -1, h, c, e, w);
            if (dist < distance) {
                distance = dist;
            }
        }

        if (hDiff > 0) {
            float time = (1 - hO) / hDiff;
            float t = tO + tDiff * time;
            float c = cO + cDiff * time;
            float e = eO + eDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, 1, c, e, w);
            if (dist < distance) {
                distance = dist;
            }
        } else if (hDiff < 0) {
            float time = (1 + hO) / hDiff;
            float t = tO + tDiff * time;
            float c = cO + cDiff * time;
            float e = eO + eDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, -1, c, e, w);
            if (dist < distance) {
                distance = dist;
            }
        }

        if (cDiff > 0) {
            float time = (1 - cO) / cDiff;
            float t = tO + tDiff * time;
            float h = hO + hDiff * time;
            float e = eO + eDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, h, 1, e, w);
            if (dist < distance) {
                distance = dist;
            }
        } else if (cDiff < 0) {
            float time = (1 + cO) / cDiff;
            float t = tO + tDiff * time;
            float h = hO + hDiff * time;
            float e = eO + eDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, h, -1, e, w);
            if (dist < distance) {
                distance = dist;
            }
        }

        if (eDiff > 0) {
            float time = (1 - eO) / eDiff;
            float t = tO + tDiff * time;
            float h = hO + hDiff * time;
            float c = cO + cDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, h, c, 1, w);
            if (dist < distance) {
                distance = dist;
            }
        } else if (eDiff < 0) {
            float time = (1 + eO) / eDiff;
            float t = tO + tDiff * time;
            float h = hO + hDiff * time;
            float c = cO + cDiff * time;
            float w = wO + wDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, h, c, -1, w);
            if (dist < distance) {
                distance = dist;
            }
        }

        if (wDiff > 0) {
            float time = (1 - wO) / wDiff;
            float t = tO + tDiff * time;
            float h = hO + hDiff * time;
            float c = cO + cDiff * time;
            float e = eO + eDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, h, c, e, 1);
            if (dist < distance) {
                distance = dist;
            }
        } else if (wDiff < 0) {
            float time = (1 + wO) / wDiff;
            float t = tO + tDiff * time;
            float h = hO + hDiff * time;
            float c = cO + cDiff * time;
            float e = eO + eDiff * time;
            float dist = distanceSquare(
                tO, hO, cO, eO, wO,
                t, h, c, e, -1);
            if (dist < distance) {
                distance = dist;
            }
        }

        if (distance == Float.MAX_VALUE) {
            return 0;
        }

        float euDistanceToEdge = Mth.sqrt(distance);

        float euDistanceToCenter = Mth.sqrt(distanceSquare(
            tO, hO, cO, eO, wO,
            tC, hC, cC, eC, wC
        ));

        return euDistanceToCenter / (euDistanceToEdge + euDistanceToCenter);
    }

    private static float distanceSquare(
        float t1, float h1, float c1, float e1, float w1,
        float t2, float h2, float c2, float e2, float w2
    ) {
        return Mth.square(t1 - t2)
            + Mth.square(h1 - h2)
            + Mth.square(c1 - c2)
            + Mth.square(e1 - e2)
            + Mth.square(w1 - w2);
    }

    public float volume() {
        return relativeVolume;
    }

    @Override
    public String toString() {
        return "TargetTransformer{" +
            "temperatureCenter=" + temperatureCenter +
            ", humidityCenter=" + humidityCenter +
            ", continentalnessCenter=" + continentalnessCenter +
            ", erosionCenter=" + erosionCenter +
            ", weirdnessCenter=" + weirdnessCenter +
            ", depthStart=" + depthStart +
            ", depthEnd=" + depthEnd +
            ", spread=" + relativeSpread +
            '}';
    }
}
