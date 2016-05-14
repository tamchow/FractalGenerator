package in.tamchow.fractal.fractals;
import java.io.Serializable;
/**
 * Interface which indicates that an implementor can generate a fractal
 */
public abstract class FractalGenerator implements Serializable {
    protected volatile boolean stop, pause;
    protected FractalGenerator() {
    }
    public abstract void generate();
    public void stop() {
        stop = true;
    }
    public synchronized void pause() {
        pause = true;
        Thread.currentThread().interrupt();
    }
    public synchronized void resume() {
        pause = false;
        notify();
    }
    protected void checkAndDoPause() {
        if (!pause) {
            return;
        }
        try {
            // synchronize with the common monitor lock of all execution threads
            synchronized (this) {
                // wait while we are interrupted or have been asked to pause execution
                do {
                    // we wait after this thread has been interrupted
                    wait();
                } while ((pause || Thread.currentThread().isInterrupted()) &&
                        // we should be on the correct thread
                        // and that thread should be alive
                        Thread.currentThread().isAlive());
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
}