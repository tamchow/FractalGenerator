package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Multithreading for the fractal generator
 */
public class ThreadedComplexFractalGenerator {
    boolean[] progress;
    ComplexFractalGenerator master;
    long iterations;
    double escape_radius;
    Complex constant;
    int nx, ny;
    public ThreadedComplexFractalGenerator(int x_threads, int y_threads, ComplexFractalGenerator master, int iterations, double escape_radius, Complex constant) {
        this.master = master;
        this.iterations = iterations;
        this.escape_radius = escape_radius;
        this.constant = constant;
        nx = x_threads;
        ny = y_threads;
        progress = new boolean[nx * ny];
    }
    public ThreadedComplexFractalGenerator(ComplexFractalGenerator master, ComplexFractalParams config) {
        this.master = master;
        this.iterations = config.runParams.iterations;
        this.escape_radius = config.runParams.escape_radius;
        this.constant = config.runParams.constant; nx = config.x_threads; ny = config.y_threads;
        progress = new boolean[nx * ny];
    }
    public void generate() {
        int idx = 0;
        for (int i = 0; i < ny; i++) {
            for (int j = 0; j < nx; j++) {
                int[] coords = master.start_end_coordinates(nx, j, ny, i);
                SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1], coords[2], coords[3]); runner.start();
                master.getProgressPublisher().println("Initiated thread: " + idx); idx++;
            }
        } try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {master.getProgressPublisher().println("Interrupted:" + e.getMessage());}
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
        ComplexFractalGenerator copyOfMaster;
        Thread executor;
        int index;
        int startx, starty, endx, endy;
        public SlaveRunner(int index, int startx, int starty, int endx, int endy) {
            this.index = index;
            this.startx = startx;
            this.starty = starty;
            this.endx = endx;
            this.endy = endy;
            this.copyOfMaster = new ComplexFractalGenerator(master.getParams(), master.getProgressPublisher());
        }
        public void start() {
            if (executor == null) {
                executor = new Thread(this);
            }
            executor.start();
        }
        public void run() {
            copyOfMaster.generate(startx, endx, starty, endy, (int) iterations, escape_radius, constant);
            onCompletion();
        }
        void onCompletion() {
            System.out.println("Thread " + index + " has completed");
            progress[index] = true;
        }
    }
}
