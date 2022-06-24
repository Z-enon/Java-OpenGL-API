package com.xenon.utils;

public class MathsTools {

    /**
     * Returns <code>i</code> rounded to a power of two.
     * @param i the integer in
     * @return <code>i</code> rounded to a power of two
     */
    public static int roundPowerOfTwo(int i) {
        int highest_one = Integer.highestOneBit(i);
        return i == highest_one ? i : highest_one << 1;
    }
}
