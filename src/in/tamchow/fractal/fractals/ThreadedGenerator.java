package in.tamchow.fractal.fractals;
/**
 * Abstract superclass for threaded fractal generator
 */
public abstract class ThreadedGenerator {
    protected final Object lock = new Lock();
    protected SlaveRunner[] threads;
    protected int currentlyCompletedThreads;
    public abstract int countCompletedThreads();
    public abstract boolean allComplete();
    public abstract void generate();
    public abstract void finalizeGeneration();
    public void wrapUp() throws InterruptedException {
        synchronized (lock) {
            while (!allComplete()) {
                lock.wait(1000);
            }
            lock.notifyAll();
            finalizeGeneration();
        }
    }
    public void resume() throws InterruptedException {
        synchronized (lock) {
            for (SlaveRunner runner : threads) {
                runner.resumeAfterPause();
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
    public abstract class SlaveRunner extends Thread {
        public Thread executor;
        public int index;
        public boolean pause, stop;
        public SlaveRunner(int index) {
            this.index = index;
        }
        @Override
        public void start() {
            if (executor == null) {
                executor = new Thread(this);
                pause = false;
                stop = false;
            }
            executor.start();
        }
        public synchronized void pause() throws InterruptedException {
            pause = true;
            stop = false;
            // interrupt the execution thread
            executor.interrupt();
            // call run() so it can check and pause execution
            run();
        }
        public synchronized void resumeAfterPause() throws InterruptedException {
            pause = false;
            stop = false;
            // notify the waiting thread that it does not need to wait any more
            lock.notify();
            // call run() so it can check and resume execution
            run();
        }
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            if (executor == null) {
                stop = true;
                return;
            }
            while (executor == thisThread && (!stop)) {
                generate();
                //break;
                // pause if we are supposed to
                checkAndDoPause(thisThread);
            }
        }
        public void checkAndDoPause(Thread thisThread) {
            try {
                // synchronize with the common monitor lock of all execution threads
                synchronized (lock) {
                    // wait while we are interrupted or have been asked to pause execution
                    do {
                        if (executor == null || thisThread == null) {
                            stop = true;
                            return;
                        }
                        // we wait after this thread has been interrupted
                        lock.wait();
                    } while ((pause || executor.isInterrupted()) &&
                            // we should be on the correct thread
                            // and that thread should be alive
                            (executor == thisThread && executor.isAlive()));
                }
            } catch (InterruptedException interrupt) {
                // set the interrupted flag of the current thread
                Thread.currentThread().interrupt();
                // this was unexpected,
                // as we don't get an interrupt while waiting but the other way round,
                // so stop execution altogether
                stop = true;
            }
        }
        public abstract void generate();
        public void onCompletion() {
            stop = true;
            executor = null;
            onCompleted();
        }
        public abstract void onCompleted();
    }
}