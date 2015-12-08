package in.tamchow.fractal;
import in.tamchow.fractal.config.fractalconfig.FractalParams;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Multithreading for the fractal generator
 * TODO: Lockup somewhere, no output. To check later.
 */
public class ThreadedGenerator {
    boolean[]        progress;
    FractalGenerator master;
    int              iterations;
    double           escape_radius;
    Complex          constant;
    int              nx, ny;
    public ThreadedGenerator(int x_threads, int y_threads, FractalGenerator master, int iterations, double escape_radius, Complex constant) {
        this.master = master;
        this.iterations = iterations;
        this.escape_radius = escape_radius;
        this.constant = constant;
        nx = x_threads;
        ny = y_threads;
        progress = new boolean[nx * ny];
    }
    public ThreadedGenerator(int x_threads, int y_threads, FractalGenerator master, FractalParams config) {
        this.master = master;
        this.iterations = config.runParams.iterations;
        this.escape_radius = config.runParams.escape_radius;
        this.constant = config.runParams.constant;
        nx = x_threads;
        ny = y_threads;
        progress = new boolean[nx * ny];
    }
    public void generate() {
        int idx = 0;
        for (int i = 0; i < ny; i++) {
            for (int j = 0; j < nx; j++) {
                int[]       coords = master.start_end_coordinates(nx, j, ny, i);
                SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[2], coords[1], coords[3]);
                System.out.println("Initiated thread " + idx);
                idx++;
            }
        }
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Interrupted:" + e.getMessage());
        }
        /*while (!allComplete()) {
            for (idx = 0; idx < progress.length; idx++) {
                //System.out.println("Thread "+idx+" has completed:"+progress[idx]);
            }
        }*/
    }
    boolean allComplete() {
        for (boolean progression : progress) {
            if (!progression) {
                return false;
            }
        }
        return true;
    }
    class SlaveRunner extends Thread {
        Thread executor;
        int    index;
        int    startx, starty, endx, endy;
        public SlaveRunner(int index, int startx, int starty, int endx, int endy) {
            this.index = index;
            this.startx = startx;
            this.starty = starty;
            this.endx = endx;
            this.endy = endy;
        }
        public void start() {
            if (executor == null) {
                executor = new Thread(this);
            }
            executor.start();
        }
        public void run() {
            master.generate(startx, endx, starty, endy, iterations, escape_radius, constant);
            onCompletion();
        }
        void onCompletion() {
            System.out.println("Thread " + index + " has completed");
            progress[index] = true;
        }
    }
}
