package in.tamchow.fractal.helpers.math;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * A PseudoRandom Number Generator (PRNG) based on the Mersenne Twister algorithm.
 * <br>
 * This does not guarantee concurrent usability of a single instance as no methods are synchronized.
 * <br>
 * Adapted from:
 * <a href="https://cs.gmu.edu/~sean/research/mersenne/MersenneTwisterFast.java">Sean Luke's Fast Mersenne Twister</a>
 * <br>
 * <br>
 * <br>
 * <ol>
 * <li>Modern documentation comments and random-in-range methods Copyright &copy; 2016 by Tamoghna Chowdhury</li>
 * </ol>
 * <br>
 * <h3>License</h3>
 * <br>
 * Copyright &copy; 2003 by Sean Luke. <br>
 * Portions copyright &copy; 1993 by Michael Lecuyer. <br>
 * All rights reserved. <br>
 * <br>
 * <br>Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li> Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <li> Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <li> Neither the name of the copyright owners, their employers, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * </ul>
 * <br>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Sean Luke, Tamoghna Chowdhury
 * @version 23
 */
public final class MersenneTwister extends java.util.Random implements Serializable, Cloneable {
    /**
     * Serialization identifier constant
     */
    private static final long serialVersionUID = -8219700664442619525L;
    /**
     * Period parameters
     */
    private static final int N = 624;
    private static final int M = 397;
    /**
     * <code>private static final</code> * constant vector {@code a}
     */
    private static final int MATRIX_A = 0x9908b0df;
    /**
     * Most significant w-r bits
     */
    private static final int UPPER_MASK = 0x80000000;
    /**
     * Least significant r bits
     */
    private static final int LOWER_MASK = 0x7fffffff;
    /**
     * Tempering parameters
     */
    private static final int TEMPERING_MASK_B = 0x9d2c5680;
    private static final int TEMPERING_MASK_C = 0xefc60000;
    /**
     * The array for the state vector
     */
    private int mt[];
    /**
     * <code>mti==N+1</code> means {@code mt[N]} is not initialized
     */
    private int mti;
    private int mag01[];
    // a good initial seed (of int size, though stored in a long)
    //private static final long GOOD_SEED = 4357;
    /**
     * Gaussian distribution indicator
     */
    private double __nextNextGaussian;
    /**
     * Gaussian distribution indicator
     */
    private boolean __haveNextNextGaussian;

    /**
     * Constructor using the default seed.
     */
    public MersenneTwister() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructor using a given seed.  Though you pass this seed in
     * as a long, it's best to make sure it's actually an integer.
     *
     * @param seed the seed of this PRNG
     * @see MersenneTwister#setSeed(long)
     */
    public MersenneTwister(long seed) {
        setSeed(seed);
    }

    /**
     * Constructor using an array of integers as seed.
     * Your array must have a non-zero length.  Only the first 624 integers
     * in the array are used; if the array is shorter than this then
     * integers are repeatedly used in a wrap-around fashion.
     *
     * @param array the seed of this PRNG
     * @see MersenneTwister#setSeed(int[])
     */
    public MersenneTwister(int[] array) {
        setSeed(array);
    }

    /**
     * We're overriding all internal data, to my knowledge, so this should be okay
     *
     * @return a clone of this {@link MersenneTwister} as an {@link Object}
     */
    @Override
    public Object clone() {
        try {
            MersenneTwister f = (MersenneTwister) (super.clone());
            f.mt = mt.clone();
            f.mag01 = mag01.clone();
            return f;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        } // should never happen
    }

    /**
     * Returns true if this {@link MersenneTwister}'s current internal state is equal to another {@link MersenneTwister}.
     * That another is {@code other}.
     * <br>
     * This is roughly the same as {@link MersenneTwister#equals(Object)}, except that it compares based on value but does not
     * guarantee the contract of immutability (obviously random number generators are immutable).
     * <br>
     * Note that this does NOT check to see if the internal gaussian storage is the same
     * for both.
     * <br>
     * You can guarantee that the internal Gaussian storage is the same (and so the
     * {@link MersenneTwister#nextGaussian()} methods will return the same values)
     * by calling {@link MersenneTwister#clearGaussian()} on both objects.
     *
     * @param other the {@link MersenneTwister} to compare with this
     * @return true if the state of {@code other} is the same as this, false otherwise
     */
    public boolean stateEquals(MersenneTwister other) {
        if (other == this) return true;
        if (other == null) return false;
        if (mti != other.mti) return false;
        for (int x = 0; x < mag01.length; x++)
            if (mag01[x] != other.mag01[x]) return false;
        for (int x = 0; x < mt.length; x++)
            if (mt[x] != other.mt[x]) return false;
        return true;
    }

    /**
     * Reads the entire state of the {@link MersenneTwister} RNG from the stream
     *
     * @param stream the {@link DataInputStream} to load the {@link MersenneTwister} from.
     * @throws IOException on any I/O error
     */
    public void readState(DataInputStream stream) throws IOException {
        int len = mt.length;
        for (int x = 0; x < len; x++) mt[x] = stream.readInt();
        len = mag01.length;
        for (int x = 0; x < len; x++) mag01[x] = stream.readInt();
        mti = stream.readInt();
        __nextNextGaussian = stream.readDouble();
        __haveNextNextGaussian = stream.readBoolean();
    }

    /**
     * Writes the entire state of the {@link MersenneTwister} RNG to the stream
     *
     * @param stream the {@link DataOutputStream} to write this {@link MersenneTwister} to.
     * @throws IOException on any I/O error
     */
    public void writeState(DataOutputStream stream) throws IOException {
        int len = mt.length;
        for (int x = 0; x < len; x++) stream.writeInt(mt[x]);
        len = mag01.length;
        for (int x = 0; x < len; x++) stream.writeInt(mag01[x]);
        stream.writeInt(mti);
        stream.writeDouble(__nextNextGaussian);
        stream.writeBoolean(__haveNextNextGaussian);
    }

    /**
     * Initialize the pseudo random number generator.  Don't
     * pass in a long that's bigger than an int (Mersenne Twister
     * only uses the first 32 bits for its seed).
     *
     * @param seed the seed for this PRNG
     */
    @Override
    public void setSeed(long seed) {
        // Due to a bug in java.util.Random clear up to 1.2, we're
        // doing our own Gaussian variable.
        __haveNextNextGaussian = false;
        mt = new int[N];
        mag01 = new int[2];
        mag01[0] = 0x0;
        mag01[1] = MATRIX_A;
        mt[0] = (int) (seed);
        for (mti = 1; mti < N; mti++) {
            mt[mti] =
                    (1812433253 * (mt[mti - 1] ^ (mt[mti - 1] >>> 30)) + mti);
            /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
            /* In the previous versions, MSBs of the seed affect   */
            /* only MSBs of the array mt[].                        */
            /* 2002/01/09 modified by Makoto Matsumoto             */
            // mt[mti] &= 0xffffffff;
            /* for >32 bit machines */
        }
    }

    /**
     * Sets the seed of the {@link MersenneTwister} using an array of integers.
     * Your array must have a non-zero length.  Only the first 624 integers
     * in the array are used; if the array is shorter than this then
     * integers are repeatedly used in a wrap-around fashion.
     *
     * @param array the seed for this PRNG
     */
    public void setSeed(int[] array) {
        if (array.length == 0)
            throw new IllegalArgumentException("Array length must be greater than zero");
        int i, j, k;
        setSeed(19650218);
        i = 1;
        j = 0;
        k = (N > array.length ? N : array.length);
        for (; k != 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1664525)) + array[j] + j; /* non linear */
            // mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
            i++;
            j++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
            if (j >= array.length) j = 0;
        }
        for (k = N - 1; k != 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1566083941)) - i; /* non linear */
            // mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
            i++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
        }
        mt[0] = 0x80000000; /* MSB is 1; assuring non-zero initial array */
    }

    /**
     * @return a random integer  drawn uniformly from 0 to {@link Integer#MAX_VALUE}
     */
    @Override
    public int nextInt() {
        return getOperated();
    }

    /**
     * @return a random {@code short} value  drawn uniformly from 0 to {@link Short#MAX_VALUE}
     */
    public short nextShort() {
        return (short) (getOperated() >>> 16);
    }

    /**
     * @return a random UTF-16 character as an {@code char} value drawn uniformly from {@code '\0'} to {@link Character#MAX_VALUE}
     */
    public char nextChar() {
        return (char) (getOperated() >>> 16);
    }

    /**
     * Simulates a fair coin toss.
     *
     * @return a {@code boolean} with equal probability of either {@code true} or {@code false}
     */
    @Override
    public boolean nextBoolean() {
        return nextBoolean(0.5);
    }

    /**
     * This generates a coin flip with a probability {@code probability}
     * of returning {@code true}, else returning {@code false}.
     *
     * @param probability the probability of returning {@code true}
     * @return {@code true} or {@code false} as regards the {@code probability}
     * @throws IllegalArgumentException if {@code probability} is not between 0.0 and 1.0, inclusive
     */
    public boolean nextBoolean(double probability) {
        if (probability < 0.0 || probability > 1.0)
            throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
        if (probability == 0.0) return false;             // fix half-open issues
        else if (probability == 1.0) return true;
        return ((((long) (getOperated() >>> 6)) << 27) + (getOperated() >>> 5)) / (double) (1L << 53) < probability;
    }

    /**
     * @return a random {@code byte} value drawn uniformly from 0 to {@link Byte#MAX_VALUE}
     */
    public byte nextByte() {
        return (byte) (getOperated() >>> 24);
    }

    /**
     * Fills the provided {@code byte[]}{@code bytes} with random byte values
     *
     * @param bytes the {@code byte[]} to fill with random bytes
     * @see MersenneTwister#nextByte()
     */
    @Override
    public void nextBytes(byte[] bytes) {
        for (int x = 0; x < bytes.length; x++) {
            bytes[x] = nextByte();
        }
    }

    /**
     * @return a {@code long} drawn uniformly from 0 to {@link Long#MAX_VALUE}-1
     */
    @Override
    public long nextLong() {
        return nextLong(Long.MAX_VALUE);
    }

    /**
     * @param min the lower limit
     * @param max the upper limit
     * @return a {@code long} drawn uniformly from {@code min} to {@code max-1}.
     */
    public long nextLong(long min, long max) {
        long diff = Math.abs(max - min);
        return nextLong(diff) + (min < max ? min : max);
    }

    /**
     * @param n the upper limit
     * @return a {@code long} drawn uniformly from 0 to n-1.
     * @throws IllegalArgumentException if {@code n} &lt; 0
     */
    public long nextLong(long n) {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive, got: " + n);
        long bits, val;
        do {
            int y = getOperated(), z = getOperated();
            bits = (((((long) y) << 32) + (long) z) >>> 1);
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    /**
     * @param max the upper limit
     * @return random {@code double} in the range {@code 0} and {@code max}
     */
    public double nextDouble(double max) {
        return nextDouble(0, max);
    }

    /**
     * @param min the lower limit
     * @param max the upper limit
     * @return random {@code double} in the range {@code min} and {@code max}
     */
    public double nextDouble(double min, double max) {
        double diff = Math.abs(max - min);
        return nextDouble(true, true) * diff + (min < max ? min : max);
    }

    /**
     * @return a random {@code double} in the half-open range from [0.0,1.0).
     * Thus, 0.0 is a valid result, but 1.0 is not.
     */
    @Override
    public double nextDouble() {
        int y = getOperated(), z = getOperated();
        /* derived from nextDouble documentation in jdk 1.2 docs, see top */
        return ((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53);
    }

    /**
     * Returns a double in the range from 0.0 to 1.0, possibly inclusive of 0.0 and 1.0 themselves.  Thus:
     * <br>
     * <table border=0>
     * <tr><th>Expression</th><th>Interval</th></tr>
     * <tr><td>nextDouble(false, false)</td><td>(0.0, 1.0)</td></tr>
     * <tr><td>nextDouble(true, false)</td><td>[0.0, 1.0)</td></tr>
     * <tr><td>nextDouble(false, true)</td><td>(0.0, 1.0]</td></tr>
     * <tr><td>nextDouble(true, true)</td><td>[0.0, 1.0]</td></tr>
     * <caption>Table of intervals</caption>
     * </table>
     * <br>
     * <br>This version preserves all possible random values in the double range.
     *
     * @param includeOne  whether to include 1.0d
     * @param includeZero whether to include 0.0d
     * @return an {@code double} in the range [0.0, 1.0], (0.0, 1.0), [0.0, 1.0) or (0.0,1.0] as above.
     */
    public double nextDouble(boolean includeZero, boolean includeOne) {
        double d = 0.0;
        do {
            d = nextDouble();                           // grab a value, initially from half-open [0.0, 1.0)
            if (includeOne && nextBoolean()) d += 1.0;  // if includeOne, with 1/2 probability, push to [1.0, 2.0)
        }
        while ((d > 1.0) ||                            // everything above 1.0 is always invalid
                (!includeZero && d == 0.0));            // if we're not including zero, 0.0 is invalid
        return d;
    }

    /**
     * Clears the internal gaussian variable from the RNG.  You only need to do this
     * in the rare case that you need to guarantee that two RNGs have identical internal
     * state.  Otherwise, disregard this method.
     *
     * @see MersenneTwister#stateEquals(MersenneTwister)
     */
    public void clearGaussian() {
        __haveNextNextGaussian = false;
    }

    /**
     * @return a random {@code double value} in a Gaussian distribution
     */
    public double nextGaussian() {
        if (__haveNextNextGaussian) {
            __haveNextNextGaussian = false;
            return __nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                int y = getOperated(), z = getOperated(), a = getOperated(), b = getOperated();
                /* derived from nextDouble documentation in jdk 1.2 docs, see top */
                v1 = 2 * (((((long) (y >>> 6)) << 27) + (z >>> 5)) / (double) (1L << 53)) - 1;
                v2 = 2 * (((((long) (a >>> 6)) << 27) + (b >>> 5)) / (double) (1L << 53)) - 1;
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
            __nextNextGaussian = v2 * multiplier;
            __haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    /**
     * @param min the lower limit
     * @param max the upper limit
     * @return random {@code float} in the range {@code min} and {@code max}
     */
    public float nextFloat(float min, float max) {
        float diff = Math.abs(max - min);
        return nextFloat() * diff + (min < max ? min : max);
    }

    /**
     * @return a random {@code float} in the half-open range from [0.0f,1.0f).
     * Thus 0.0f is a valid result but 1.0f is not.
     */
    @Override
    public float nextFloat() {
        return (getOperated() >>> 8) / ((float) (1 << 24));
    }

    /**
     * Returns a float in the range from 0.0f to 1.0f, possibly inclusive of 0.0f and 1.0f themselves.  Thus:
     * <br>
     * <table border=0>
     * <tr><th>Expression</th><th>Interval</th></tr>
     * <tr><td>nextFloat(false, false)</td><td>(0.0f, 1.0f)</td></tr>
     * <tr><td>nextFloat(true, false)</td><td>[0.0f, 1.0f)</td></tr>
     * <tr><td>nextFloat(false, true)</td><td>(0.0f, 1.0f]</td></tr>
     * <tr><td>nextFloat(true, true)</td><td>[0.0f, 1.0f]</td></tr>
     * <caption>Table of intervals</caption>
     * </table>
     * <br>
     * <br>This version preserves all possible random values in the float range.
     *
     * @param includeOne  whether to include 1.0f
     * @param includeZero whether to include 0.0f
     * @return an {@code float} in the range [0.0, 1.0], (0.0, 1.0), [0.0, 1.0) or (0.0,1.0] as above.
     */
    public float nextFloat(boolean includeZero, boolean includeOne) {
        float d;
        do {
            d = nextFloat();                            // grab a value, initially from half-open [0.0f, 1.0f)
            if (includeOne && nextBoolean()) d += 1.0f; // if includeOne, with 1/2 probability, push to [1.0f, 2.0f)
        }
        while ((d > 1.0f) ||                           // everything above 1.0f is always invalid
                (!includeZero && d == 0.0f));           // if we're not including zero, 0.0f is invalid
        return d;
    }

    /**
     * @param min the lower limit
     * @param max the upper limit
     * @return an integer drawn uniformly from {@code min} to {@code max-1}.
     */
    public int nextInt(int min, int max) {
        int diff = Math.abs(max - min);
        return nextInt(diff) + (min < max ? min : max);
    }

    /**
     * @param n the upper limit
     * @return an integer drawn uniformly from 0 to n-1.
     * @throws IllegalArgumentException if {@code n} &lt; 0
     */
    @Override
    public int nextInt(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive, got: " + n);
        if (n % 2 == 0) {
            int y = getOperated();
            return (int) ((n * (long) (y >>> 1)) >> 31);
        }
        int bits, val;
        do {
            int y = getOperated();
            bits = (y >>> 1);
            val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
    }

    private int getOperated() {
        int y;
        if (mti >= N)   // generate N words at one time
        {
            int kk;
            final int[] mt = this.mt; // locals are slightly faster
            final int[] mag01 = this.mag01; // locals are slightly faster
            for (kk = 0; kk < N - M; kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            for (; kk < N - 1; kk++) {
                y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
            }
            y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];
            mti = 0;
        }
        y = mt[mti++];
        y ^= y >>> 11;                          // TEMPERING_SHIFT_U(y)
        y ^= (y << 7) & TEMPERING_MASK_B;       // TEMPERING_SHIFT_S(y)
        y ^= (y << 15) & TEMPERING_MASK_C;      // TEMPERING_SHIFT_T(y)
        y ^= (y >>> 18);                        // TEMPERING_SHIFT_L(y)
        return y;
    }
}
