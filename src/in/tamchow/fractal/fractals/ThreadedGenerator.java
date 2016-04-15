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
    public void wrapUp() {
        try {
            synchronized (lock) {
                while (!allComplete()) {
                    lock.wait(1000);
                }
                lock.notifyAll();
                finalizeGeneration();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //master.getProgressPublisher().println("Exception:" + e.getMessage());
        }
    }
    public void resume() {
        synchronized (lock) {
            for (SlaveRunner runner : threads) {
                runner.resumeAfterPause();
            }
        }
        //generate();
    }
    public void pause() {
        currentlyCompletedThreads = countCompletedThreads();
        synchronized (lock) {
            for (SlaveRunner runner : threads) {
                runner.pause();
            }
            try {
                Thread.currentThread().join();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }
    private static final class Lock {
    }
    public abstract class SlaveRunner extends Thread {
        public Thread executor;
        public int index;
        public boolean running;
        public SlaveRunner(int index) {
            this.index = index;
        }
        public void start() {
            if (executor == null) {
                executor = new Thread(this);
                running = true;
            }
            executor.start();
        }
        public synchronized void pause() {
            running = false;
            executor.interrupt();
        }
        public synchronized void resumeAfterPause() {
            running = true;
            notifyAll();
        }
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            while (executor == thisThread && running) {
                try {
                    generate();
                    //break;
                    synchronized (this) {
                        while (((!running) || executor.isInterrupted()) &&
                                (executor == thisThread && executor.isAlive())) {
                            wait();
                        }
                    }
                } catch (InterruptedException interrupt) {
                    running = false;
                }
            }
        }
        public abstract void generate();
        public void onCompletion() {
            running = false;
            executor = null;
            onCompleted();
        }
        public abstract void onCompleted();
    }
}