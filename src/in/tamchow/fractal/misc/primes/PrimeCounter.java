package in.tamchow.fractal.misc.primes;
import org.jetbrains.annotations.NotNull;
/**
 * PrimeCounter - an application/library to calculate the number of prime numbers below a certain number,
 * also mathematically known as pi(x), where x is a natural number greater than one.
 * <p/>
 * Uses sieves and multithreaded and optimized trial division.
 * <p/>
 * Timing facility is built-in.
 * <p/>
 * This is public domain software, licensed under the Apache License 2.0
 *
 * @author Tamoghna Chowdhury
 * @version 1.3
 * @since 2016-03-02
 */
public class PrimeCounter {
    /**
     * Some Strings used for I/O purposes, factored out of code for i18n purposes.
     */
    public static final String START_CODE = "=", TEST_FORMAT = "Input = %d , Output = %d , calculated in %f seconds%n", PROMPT = "Enter numbers to compute pi(x) for (Type \"" + START_CODE + "\" to start):%n", WAIT = "Calculating, please wait...%n", WARNING = "Probably won't work with values close to or more than 2^31%n", TOTAL_OUTPUT_FORMAT = "Total time for all inputs is %f seconds%n";
    /**
     * A constant object of {@link PrimeCounter.Lock}, used for multithreading purposes.
     *
     * @see PrimeCounter.Lock
     */
    @SuppressWarnings("JavaDoc")
    private static final Object LOCK = new Lock();
    /**
     * Some necessary (and unnecessary) constants.
     * <p/>
     * For further documentation, see {@link #PrimeCounter(int, int, int, int, int)}
     */
    public final int NUM_THREADS, LOW_LIM, SPLIT_LIM, HIGH_LIM, REDUCE_FACTOR;
    /**
     * Default constructor. Sets default values for the parameters.
     * The values used are experimentally determined for best performance on the author's system.
     */
    public PrimeCounter() {
        this(0xF, 0x8_000, 0x80, 0x1_000_0000, 0x4);
    }
    /**
     * Parameterized Constructor - configures calculation parameters
     *
     * @param NUM_THREADS   : The number of threads to be used in multithreaded trial division.
     * @param LOW_LIM       : The minimum value of 'x' for which trial division will be used.
     * @param SPLIT_LIM     : The value which determines how many threads will be used. {@link #NUM_THREADS}
     * @param HIGH_LIM      : The value of 'x' for which the {@link #bitPrimeSieve(long)} will be used.
     * @param REDUCE_FACTOR : The value which determines the packing density {@link #bitPrimeSieve(long)} will use.
     */
    public PrimeCounter(int NUM_THREADS, int LOW_LIM, int SPLIT_LIM, int HIGH_LIM, int REDUCE_FACTOR) {
        this.NUM_THREADS = NUM_THREADS;
        this.LOW_LIM = LOW_LIM;
        this.SPLIT_LIM = SPLIT_LIM;
        this.HIGH_LIM = HIGH_LIM;
        this.REDUCE_FACTOR = REDUCE_FACTOR;
    }
    /**
     * Main method: accepts user input and shows total execution time taken
     *
     * @param args : The command-line arguments
     */
    public static void main(String[] args) {
        double total_time = 0;
        @NotNull java.util.Scanner sc = new java.util.Scanner(System.in);
        @NotNull java.util.ArrayList<Long> numbers = new java.util.ArrayList<>();
        System.out.format(PROMPT + WARNING);
        String line = sc.nextLine();
        while (!line.equals(START_CODE)/*sc.hasNextLine()&&Character.isDigit(line.charAt(0))*/) {
            numbers.add(Long.valueOf(line));
            line = sc.nextLine();
        }
        System.out.format(WAIT);
        for (long num : numbers) {
            total_time += sieveTest(num);
        }
        System.out.format(TOTAL_OUTPUT_FORMAT, total_time / 1e9);
    }
    /**
     * Private testing and timer function
     *
     * @param MAX : input to be passed on to {@link #primeSieve(long)}
     * @return The time taken for the calculation of pi(x)
     */
    private static long sieveTest(long MAX) {
        long start = System.nanoTime();
        long ps = new PrimeCounter().primeSieve(MAX);
        long end = System.nanoTime();
        System.out.format(TEST_FORMAT, MAX, ps, ((end - start) / 1E9));
        return end - start;
    }
    /**
     * Checks for completion of threads
     *
     * @param array : The array containing the completion data
     * @return True if completed, false if not
     */
    private static boolean completed(@NotNull long[] array) {
        for (long i : array) {
            if (i < 0) return false;
        }
        return true;
    }
    /**
     * Checks if the parameter is prime or not.
     * 2,3,5,7 are hardcoded as factors.
     *
     * @param n : the number to check for primality
     * @return True if n is prime, false if not
     */
    private static boolean isPrime(long n) {
        if (n == 2 || n == 3 || n == 5 || n == 7) return true;
        else if (n % 2 == 0 || n % 3 == 0 || n % 5 == 0 || n % 7 == 0) return false;
        else {
            for (long i = 11; i < n; i += 2) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }
    /**
     * Calculates primes using the atandard Sieve of Eratosthenes.
     * Uses 2,3,5,7 wheel factorization for elimination (hardcoded for performance reasons)
     *
     * @param MAX : argument x for pi(x)
     *            Will delegate to {@link #primeCount(long)} for MAX&lt;LOW_LIM,
     *            and to {@link #bitPrimeSieve(long)} for MAX&gt;HIGH_LIM, for performance reasons.
     * @return The number of prime numbers &lt;= MAX
     * @see #primeCount(long)
     * @see #bitPrimeSieve(long)
     */
    public long primeSieve(long MAX) {
        if (MAX <= 1) return 0;
        else if (LOW_LIM > 0 && MAX < LOW_LIM) {
            return primeCount(MAX);
        } else if (HIGH_LIM > 0 && MAX > HIGH_LIM) {
            return bitPrimeSieve(MAX);
        }
        int n = (int) MAX;
        int sn = (int) Math.sqrt(n), ctr = 2;
        if (sn % 2 == 0) --sn;
        @NotNull boolean[] ps = new boolean[n + 1];
        for (int i = 2; i <= n; ++i) {
            if (i == 2 || i == 3 || i == 5 || i == 7) ps[i] = true;
            else if (i % 2 != 0 && i % 3 != 0 && i % 5 != 0 && i % 7 != 0) ps[i] = true;
            else ++ctr;
        }
        for (int i = (n > 10) ? 11 : 3; i <= sn; i += 2) {
            if (ps[i]) {
                for (int j = i * i; j <= n; j += i << 1) {
                    if (ps[j]) {
                        ps[j] = false;
                        ++ctr;
                    }
                }
            }
        }
        return (n + 1 - ctr);
    }
    /**
     * Generates and counts primes using an optimized but naive iterative algorithm.
     * Uses MultiThreading for arguments above LOW_LIM
     *
     * @param MAX : Argument x for pi(x), the limit to which to generate numbers.
     * @return The number of prime numbers &lt;= MAX
     */
    public long primeCount(long MAX) {
        long ctr = 1;
        if (MAX < SPLIT_LIM) {
            for (long i = 3; i <= MAX; i += 2) {
                if (isPrime(i)) ++ctr;
            }
        } else {
            int threads = (NUM_THREADS <= 0) ? (int) MAX / SPLIT_LIM : NUM_THREADS;
            @NotNull final long[] counts = new long[threads];
            for (int i = 0; i < threads; ++i) {
                counts[i] = -1;
            }
            long range = Math.round((double) MAX / threads);
            for (int i = 0; i < threads; ++i) {
                final long start = (i == 0) ? 3 : i * range + 1, end = (i == threads - 1) ? MAX : (i + 1) * range;
                final int idx = i;
                new Thread(new Runnable() {
                    public void run() {
                        for (long j = start; j <= end; j += 2) {
                            if (isPrime(j)) ++counts[idx];
                        }
                    }
                }).start();
            }
            synchronized (LOCK) {
                while (!completed(counts)) {
                    try {
                        LOCK.wait(300);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                LOCK.notifyAll();
            }
            for (long count : counts) {
                ctr += count;
            }
            ctr += threads;
        }
        return ctr;
    }
    /**
     * Calculates primes using bitmasked Sieve of Eratosthenes.
     *
     * @param MAX : argument x for pi(x)
     * @return The number of prime numbers &lt;= MAX
     */
    public long bitPrimeSieve(long MAX) {
        long SQRT_MAX = (long) Math.sqrt(MAX);
        if (SQRT_MAX % 2 == 0) --SQRT_MAX;
        int MEMORY_SIZE = (int) ((MAX + 1) >> REDUCE_FACTOR);
        @NotNull byte[] array = new byte[MEMORY_SIZE];
        for (long i = 3; i <= SQRT_MAX; i += 2) {
            if ((array[(int) (i >> REDUCE_FACTOR)] & (byte) (1 << ((i >> 1) & 7))) == 0) {
                for (long j = i * i; j <= MAX; j += i << 1) {
                    if ((array[(int) (j >> REDUCE_FACTOR)] & (byte) (1 << ((j >> 1) & 7))) == 0) {
                        array[(int) (j >> REDUCE_FACTOR)] |= (byte) (1 << ((j >> 1) & 7));
                    }
                }
            }
        }
        long pi = 1;
        for (long i = 3; i <= MAX; i += 2) {
            if ((array[(int) (i >> REDUCE_FACTOR)] & (byte) (1 << ((i >> 1) & 7))) == 0) {
                ++pi;
            }
        }
        return pi;
    }
    private static final class Lock {
    }
}