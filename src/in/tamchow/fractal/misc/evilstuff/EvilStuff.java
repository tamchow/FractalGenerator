package in.tamchow.fractal.misc.evilstuff;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Messes up arithmetic for some cases using reflection to shuffle cache stuffs. Enjoy!
 * Accesses the "cache" field of a {@link Number} subclass object,
 * and shuffles that cache (autoboxing cache).
 * <p>
 * Currently known to work only with {@link Integer}
 * </p>
 * Ridiculous output can only be seen if int (in the range of a {@link Byte}
 * calculations are autoboxed to {@link java.lang.Integer}
 * <p>
 * Try {@link java.io.PrintStream#format(String, Object...)} of {@link System#out} for best results!
 * </p>
 * @author Tamoghna Chowdhury
 * @version 19.03.2016
 */
public class EvilStuff {
    /**
     * Local thread synchronization object
     */
    private static final Lock LOCK = new Lock();
    /**
     * Marker field for thread execution
     */
    private static volatile boolean stopDoingEvilStuff = false;
    /**
     * Holds the current number of threads created (1-based)
     */
    private static long numberOfEvilThreads = 0;
    /**
     * Does a Fisher-Yates shuffling of the input.
     *
     * @param stuff The array to shuffle
     */
    public static void shuffle(@NotNull final Object[] stuff) {
        final int length = stuff.length;
        for (int i = 0; i < length; ++i) {
            // Get a random index of the stuff past i.
            //truncation (flooring) is necessary to avoid array bounds violations
            int randomIndexForSwap = i + (int) (Math.random() * (length - i));
            // Swap the random element with the present element.
            Object randomElement = stuff[randomIndexForSwap];
            stuff[randomIndexForSwap] = stuff[i];
            stuff[i] = randomElement;
        }
    }
    /**
     * Shuffles the cache.
     * Should work for any distribution whose nested cache class index is known.
     *
     * @param victim           The class whose cache should be shuffled. Recommend {@link Integer}
     * @param cacheID          The name of the cache array in the victim class
     * @param cacheFieldNumber The index of the cache nested class
     */
    public static void doEvilStuff(@NotNull Class<?> victim, @NotNull String cacheID, int cacheFieldNumber) {
        doEvilStuff(victim, cacheID, victim.getDeclaredClasses()[cacheFieldNumber]);
    }
    /**
     * Shuffles the cache.
     * Default for OpenJDK or Oracle distributions
     *
     * @param victim  The class whose cache should be shuffled. Recommend {@link Integer}
     * @param cacheID The name of the cache array in the victim class
     */
    public static void doEvilStuff(@NotNull final Class<? extends Number> victim, @NotNull final String cacheID) {
        String cacheClassName = String.format("%s$%sCache", victim.getName(), victim.getSimpleName());
        try {
            doEvilStuff(victim, cacheID, Class.forName(cacheClassName));
        } catch (ClassNotFoundException missingClass) {
            throw new IllegalArgumentException(String.format("%s is missing class!", cacheClassName), missingClass);
        }
    }
    /**
     * Shuffles the cache.
     * Does the real work.
     *
     * @param victim  The class whose cache should be shuffled. Recommend {@link Integer}
     * @param cacheID The name of the cache array in the victim class
     * @param cache   The cache nested class
     */
    public static void doEvilStuff(final Class<?> victim, @NotNull final String cacheID, @NotNull final Class cache) {
        java.lang.reflect.Field c;
        try {
            c = cache.getDeclaredField(cacheID);
        } catch (NoSuchFieldException noCache) {
            throw new IllegalArgumentException(String.format("Can't mess with something (%s) without a cache (%s)!", victim + "", cacheID), noCache);
        }
        c.setAccessible(true);
        Object cached;
        try {
            cached = c.get(cache);
        } catch (IllegalAccessException noAccessPermit) {
            throw new IllegalArgumentException(String.format("Can't access the cache (%s) of %s!", cacheID, victim + ""), noAccessPermit);
        }
        try {
            shuffle((Object[]) cached);
        } catch (NullPointerException dataLoss) {
            throw new IllegalArgumentException("Catastrophic data loss (for this program only)!", dataLoss);
        }
    }
    /**
     * Accessor method for the thread counter
     *
     * @return long The number of threads initiated
     */
    public static long getNumberOfEvilThreads() {
        return numberOfEvilThreads;
    }
    /**
     * Users should call this to end the started threads
     *
     * @throws InterruptedException if this is interrupted during notifying
     */
    public static void stopDoingEvilStuff() throws InterruptedException {
        stopDoingEvilStuff = true;
        LOCK.notifyAll();
        Thread.currentThread().join();
    }
    /**
     * Shuffles the cache.
     * Should work for any distribution whose nested cache class index is known.
     *
     * @param victim           The class whose cache should be shuffled. Recommend {@link Integer}
     * @param cacheID          The name of the cache array in the victim class
     * @param cacheFieldNumber The index of the cache nested class
     * @param threadName       The name of the thread which will be created
     * @param waitPeriod       The time to wait in milliseconds between iterations
     */
    public static void repeatedlyDoEvilStuff(@NotNull Class<?> victim, @NotNull String cacheID, int cacheFieldNumber, final String threadName, final int waitPeriod) {
        repeatedlyDoEvilStuff(victim, cacheID, victim.getDeclaredClasses()[cacheFieldNumber], threadName, waitPeriod);
    }
    /**
     * Repeatedly shuffles the cache.
     * Default for OpenJDK or Oracle distributions
     *
     * @param victim     The class whose cache should be shuffled. Recommend {@link Integer}
     * @param cacheID    The name of the cache array in the victim class
     * @param threadName The name of the thread which will be created
     * @param waitPeriod The time to wait in milliseconds between iterations
     */
    public static void repeatedlyDoEvilStuff(@NotNull final Class<? extends Number> victim, @NotNull final String cacheID, final String threadName, final int waitPeriod) {
        String cacheClassName = String.format("%s$%sCache", victim.getName(), victim.getSimpleName());
        try {
            repeatedlyDoEvilStuff(victim, cacheID, Class.forName(cacheClassName), threadName, waitPeriod);
        } catch (ClassNotFoundException missingClass) {
            throw new IllegalArgumentException(String.format("%s is missing class!", cacheClassName), missingClass);
        }
    }
    /**
     * Repeatedly shuffles the cache.
     * Does the real work.
     *
     * @param victim     The class whose cache should be shuffled. Recommend {@link Integer}
     * @param cacheID    The name of the cache array in the victim class
     * @param cache      The cache nested class
     * @param threadName The name of the thread which will be created
     * @param waitPeriod The time to wait in milliseconds between iterations
     */
    public static void repeatedlyDoEvilStuff(final Class<?> victim, @NotNull final String cacheID, @NotNull final Class cache, final String threadName, final int waitPeriod) {
        EvilStuff.doEvilStuff(victim, cacheID, cache);
        @NotNull Thread evilThread = new Thread(new Runnable() {
            public void run() {
                while (!stopDoingEvilStuff) {
                    try {
                        synchronized (LOCK) {
                            EvilStuff.doEvilStuff(victim, cacheID, cache);
                            LOCK.wait(waitPeriod);
                        }
                    } catch (InterruptedException interrupted) {
                        interrupted.printStackTrace();
                        LOCK.notifyAll();
                    }
                }
            }
        });
        evilThread.setName(threadName + (++numberOfEvilThreads));
        evilThread.start();
    }
    /**
     * Class for local thread synchronization object
     */
    private static final class Lock {
    }
}