package in.tamchow.fractal.fractals;
import java.io.Serializable;
/**
 * Abstract superclass for threaded fractal generator
 */
public abstract class ThreadedGenerator implements Serializable {
    protected final Object lock = new Lock();
    protected volatile SlaveRunner[] threads;
    protected volatile int currentlyCompletedThreads;
    protected ThreadedGenerator() {
    }
    public abstract int countCompletedThreads();
    public abstract void generate();
    public abstract void finalizeGeneration();
    public void joinAll() throws InterruptedException {
        if (threads != null) {
            for (SlaveRunner runner : threads) {
                runner.join();
            }
            finalizeGeneration();
        }
    }
    public void resume() throws InterruptedException {
        synchronized (lock) {
            for (SlaveRunner runner : threads) {
                runner.resume();
            }
        }
        //generate();
    }
    public void pause() throws InterruptedException {
        currentlyCompletedThreads = countCompletedThreads();
        synchronized (lock) {
            for (SlaveRunner runner : threads) {
                runner.pause();
            }
            /*try {
                Thread.currentThread().join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }*/
        }
    }
    private static final class Lock {
    }
    public abstract class SlaveRunner implements Runnable {
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
        public void join() throws InterruptedException {
            executor.join();
        }
        public abstract void pause() throws InterruptedException;
        public abstract void resume() throws InterruptedException;
        @Override
        public void run() {
            generate();
        }
        public abstract void generate();
        public abstract void onCompletion();
    }
}