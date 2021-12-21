package net.protolauncher.util;

public class AnimUtil {

    /**
     * Linearly interpolates from the start value to the end value over time frac.
     * @param start The starting value.
     * @param end The ending value.
     * @param frac The frac.
     * @return The linearly interpolated value.
     */
    public static double lerp(double start, double end, double frac) {
        return (start + frac * (end - start));
    }

}
