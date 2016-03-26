package in.tamchow.fractal.fractals;

/**
 * Abstract superclass for threaded fractal generator
 */
public abstract class ThreadedGenerator {
    public final Object lock = new Lock();

    public abstract int countCompletedThreads();

    public abstract boolean allComplete();

    public abstract void generate();

    private static final class Lock {
    }

    public abstract class SlaveRunner extends Thread {
        public Thread executor;
        public int index;

        public SlaveRunner(int index) {
            this.index = index;
        }

        public void start() {
            if (executor == null) {
                executor = new Thread(this);
            }
            executor.start();
        }

        public abstract void run();

        public abstract void onCompletion();
    }
}