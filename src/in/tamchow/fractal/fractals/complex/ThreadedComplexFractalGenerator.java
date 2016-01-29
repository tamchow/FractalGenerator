package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Multithreading for the fractal generator
 */
public final class ThreadedComplexFractalGenerator implements Serializable {
    final ComplexFractalGenerator master;
    private final Object lock = new Lock();
    PartImage[] buffer;
    long iterations;
    double escape_radius;
    Complex constant;
    int nx, ny;
    public ThreadedComplexFractalGenerator(int x_threads, int y_threads, ComplexFractalGenerator master, int iterations, double escape_radius, Complex constant) {
        this.master = master; this.iterations = iterations; this.escape_radius = escape_radius;
        this.constant = constant; nx = x_threads; ny = y_threads; buffer = new PartImage[nx * ny];
    }
    public ThreadedComplexFractalGenerator(ComplexFractalGenerator master) {
        this(master, master.getParams());
    }
    public ThreadedComplexFractalGenerator(ComplexFractalGenerator master, ComplexFractalParams config) {
        this.master = master; this.iterations = config.runParams.iterations;
        this.escape_radius = config.runParams.escape_radius;
        this.constant = config.runParams.constant; nx = config.x_threads; ny = config.y_threads;
        buffer = new PartImage[nx * ny];
    }
    private int countCompletedThreads() {
        int ctr = 0; for (PartImage partImage : buffer) {if (partImage != null) ctr++;} return ctr;
    }
    public void generate() {generate(0, master.argand.getWidth(), 0, master.argand.getHeight());}
    public void generate(int startx, int endx, int starty, int endy) {
        int idx = 0;
        for (int i = 0; i < ny; i++) {
            for (int j = 0; j < nx; j++) {
                int[] coords = master.start_end_coordinates(startx, endx, starty, endy, nx, j, ny, i);
                SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1], coords[2], coords[3]);
                master.getProgressPublisher().println("Initiated thread: " + idx); idx++; runner.start();
            }
        } try {
            synchronized (lock) {
                while (!allComplete()) {lock.wait(1000);} lock.notifyAll();
                int[] histogram = new int[(int) iterations + 2]; int total = 0;
                if (master.getColor().getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || master.getColor().getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                    for (PartImage partImage : buffer) {
                        for (int i = 0; i < histogram.length; i++) {histogram[i] += partImage.histogram[i];}
                    }
                } for (int i = 0; i < iterations; i++) {total += histogram[i];}
                double scaling = Math.pow(master.zoom, master.zoom_factor); for (PartImage partImage : buffer) {
                    for (int i = partImage.starty; i < partImage.endy; i++) {
                        for (int j = partImage.startx; j < partImage.endx; j++) {
                            master.escapedata[i][j] = partImage.escapedata[i][j];
                            master.normalized_escapes[i][j] = partImage.normalized_escapes[i][j];
                            if (master.getColor().getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || master.getColor().getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                                double hue = 0.0, hue2 = 0.0, hue3 = 0.0;
                                for (int k = 0; k < partImage.escapedata[i][j]; k += 1) {
                                    hue += ((double) histogram[k]) / total;
                                } double normalized_count = partImage.normalized_escapes[i][j]; int colortmp;
                                if (master.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                                    for (int k = 0; k < master.escapedata[i][j] + 1; k += 1) {
                                        hue2 += ((double) histogram[k]) / total;
                                    } for (int k = 0; k < master.escapedata[i][j] - 1; k += 1) {
                                        hue3 += ((double) histogram[k]) / total;
                                    }
                                    int colortmp1 = ColorConfig.linearInterpolated(master.color.createIndex(hue, 0, 1, scaling), master.color.createIndex(hue2, 0, 1, scaling), normalized_count - (int) normalized_count, master.color.isByParts());
                                    int colortmp2 = ColorConfig.linearInterpolated(master.color.createIndex(hue3, 0, 1, scaling), master.color.createIndex(hue, 0, 1, scaling), normalized_count - (int) normalized_count, master.color.isByParts());
                                    colortmp = ColorConfig.linearInterpolated(colortmp2, colortmp1, normalized_count - (int) normalized_count, master.color.isByParts());
                                } else {
                                    colortmp = master.color.splineInterpolated(master.color.createIndex(hue, 0, 1, scaling), normalized_count - (int) normalized_count);
                                } master.argand.setPixel(i, j, colortmp);
                            } else {master.argand.setPixel(i, j, partImage.imageData.getPixel(i, j));}
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //master.getProgressPublisher().println("Exception:" + e.getMessage());
        }
    }
    public boolean allComplete() {
        for (PartImage partImage : buffer) {
            if (partImage == null) {return false;}
        } return true;
    }
    private static final class Lock {}
    class SlaveRunner extends Thread {
        ComplexFractalGenerator copyOfMaster;
        Thread executor;
        int index;
        int startx, starty, endx, endy;
        public SlaveRunner(int index, int startx, int endx, int starty, int endy) {
            this.index = index;
            this.startx = startx;
            this.starty = starty;
            this.endx = endx;
            this.endy = endy;
            this.copyOfMaster = new ComplexFractalGenerator(master.getParams(), master.getProgressPublisher());
        }
        public void start() {
            if (executor == null) {executor = new Thread(this);} executor.start();
        }
        public void run() {
            copyOfMaster.generate(startx, endx, starty, endy, (int) iterations, escape_radius, constant);
            onCompletion();
        }
        void onCompletion() {
            if (master.getColor().getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || master.getColor().getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                buffer[index] = new PartImage(copyOfMaster.getEscapedata(), copyOfMaster.getNormalized_escapes(), copyOfMaster.getHistogram(), startx, endx, starty, endy);
            } else {
                buffer[index] = new PartImage(copyOfMaster.getEscapedata(), copyOfMaster.getNormalized_escapes(), new ImageData(copyOfMaster.getArgand()), startx, endx, starty, endy);
            } float completion = ((float) countCompletedThreads() / (nx * ny)) * 100.0f;
            master.progressPublisher.println("Thread " + index + " has completed, total completion = " + completion + "%");
        }
    }
}