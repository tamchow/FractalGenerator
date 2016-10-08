package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;

import static in.tamchow.fractal.helpers.math.MathUtils.*;
/**
 * Multithreading for the fractal generator
 */
public final class ThreadedComplexFractalGenerator extends ThreadedGenerator {
    private final ComplexFractalGenerator master;
    private volatile PartComplexFractalData[] buffer;
    private long iterations;
    private double escape_radius;
    @Nullable
    private Complex constant;
    private int nx, ny, threads;
    public ThreadedComplexFractalGenerator(int x_threads, int y_threads, ComplexFractalGenerator master, int iterations, double escape_radius, Complex constant) {
        this.master = master;
        this.iterations = iterations;
        this.escape_radius = escape_radius;
        this.constant = constant;
        nx = x_threads;
        ny = y_threads;
        threads = nx * ny;
        buffer = new PartComplexFractalData[nx * ny];
    }
    public ThreadedComplexFractalGenerator(@NotNull ComplexFractalGenerator master) {
        this(master, master.getParams());
    }
    public ThreadedComplexFractalGenerator(ComplexFractalGenerator master, @NotNull ComplexFractalParams config) {
        this.master = master;
        this.iterations = config.runParams.iterations;
        this.escape_radius = config.runParams.escape_radius;
        this.constant = config.runParams.constant;
        nx = config.getX_threads();
        ny = config.getY_threads();
        threads = nx * ny;
        buffer = new PartComplexFractalData[nx * ny];
    }
    @Override
    public int countCompletedThreads() {
        int ctr = 0;
        for (@Nullable PartComplexFractalData partImage : buffer) {
            if (partImage != null) ctr++;
        }
        return ctr;
    }
    public void generate() {
        if (master.getParams().useThreadedGenerator()) {
            generate(0, master.getImageWidth(), 0, master.getImageHeight());
        } else {
            master.generate();
        }
    }
    public void generate(int startx, int endx, int starty, int endy) {
        int idx = 0;
        for (int t = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; t < threads; ++t) {
            @NotNull int[] coords = ComplexFractalGenerator.start_end_coordinates(startx, endx, starty, endy, nx, t % nx, ny, t / nx);
            @NotNull SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1], coords[2], coords[3]);
            master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), (float) idx / threads, idx);
            idx++;
            runner.start();
        }
        try {
            wrapUp();
        } catch (InterruptedException interrupted) {
            interrupted.printStackTrace();
        }
    }
    @Override
    public void finalizeGeneration() {
        @NotNull int[] histogram = new int[(int) iterations + 1];
        int total = 0;
        if (master.color.getMode() == Colors.MODE.HISTOGRAM || master.color.getMode() == Colors.MODE.RANK_ORDER) {
            for (@NotNull PartComplexFractalData partImage : buffer) {
                for (int i = 0; i < histogram.length; i++) {
                    histogram[i] += partImage.histogram[i];
                }
            }
        }
        for (int i = 0; i < iterations; i++) {
            total += histogram[i];
        }
        if (master.color.getMode() == Colors.MODE.RANK_ORDER) {
            System.arraycopy(rankListFromHistogram(histogram), 0, histogram, 0, histogram.length);
        }
        //double scaling = master.base_precision * Math.pow(master.zoom, master.zoom_factor);
        for (@Nullable PartComplexFractalData partImage : buffer) {
            if (partImage == null) {
                continue;
            }
            for (int i = partImage.starty; i < partImage.endy; i++) {
                for (int j = partImage.startx; j < partImage.endx; j++) {
                    master.escapedata[i][j] = partImage.escapedata[i][j];
                    master.normalized_escapes[i][j] = partImage.normalized_escapes[i][j];
                    double normalized_count = master.normalized_escapes[i][j];
                    int colortmp, pi = i, pj = j - 1, ni = i, nj = j + 1;
                    if (pj < 0) {
                        pi = (i == 0) ? i : i - 1;
                        pj = master.escapedata[pi].length - 1;
                    }
                    if (nj >= master.escapedata[i].length) {
                        ni = (i == master.escapedata.length - 1) ? i : i + 1;
                        nj = 0;
                    }
                    int ep = master.escapedata[pi][pj], en = master.escapedata[ni][nj], e = master.escapedata[i][j];
                    if (master.color.getMode() == Colors.MODE.HISTOGRAM || master.color.getMode() == Colors.MODE.RANK_ORDER) {
                        if (master.color.getMode() == Colors.MODE.RANK_ORDER) {
                            int idxp = master.color.createIndex(percentileOf(ep, histogram), 0, 1);
                            int idx = master.color.createIndex(percentileOf(e, histogram), 0, 1);
                            int idxn = master.color.createIndex(percentileOf(en, histogram), 0, 1);
                            if (master.isNonPercentileBasedRankOrder()) {
                                idxp = master.color.createIndex(((double) indexOf(histogram, ep)) / iterations, 0, 1);
                                idx = master.color.createIndex(((double) indexOf(histogram, e)) / iterations, 0, 1);
                                idxn = master.color.createIndex(((double) indexOf(histogram, en)) / iterations, 0, 1);
                            }
                            int idxMin = Math.min(idxn, idxp), idxMax = Math.max(idxn, idxp);
                            if (master.color.isLinearInterpolation()) {
                                colortmp = master.color.interpolated(idxp, idx, idxn, normalized_count - (long) normalized_count);
                            } else {
                                if (master.color.isModifierEnabled()) {
                                    colortmp = master.color.interpolated(idx, idxMin, idxMax, normalized_count - (long) normalized_count);
                                } else {
                                    if (master.color.isLogIndex()) {
                                        colortmp = master.color.interpolated(idxMin, idx, normalized_count - (long) normalized_count);
                                    } else {
                                        colortmp = master.color.interpolated(idx, idxMax, normalized_count - (long) normalized_count);
                                    }
                                }
                            }
                        } else {
                            double hue = 0.0, hue2 = 0.0, hue3 = 0.0;
                            for (int k = 0; k < e; k += 1) {
                                hue += ((double) histogram[k]) / total;
                            }
                            for (int k = 0; k < en; k += 1) {
                                hue2 += ((double) histogram[k]) / total;
                            }
                            for (int k = 0; k < ep; k += 1) {
                                hue3 += ((double) histogram[k]) / total;
                            }
                            if (master.color.isLinearInterpolation()) {
                                colortmp = master.color.interpolated(master.color.createIndex(hue2, 0, 1), master.color.createIndex(hue, 0, 1), master.color.createIndex(hue3, 0, 1), normalized_count - (long) normalized_count);
                            } else {
                                int idxp = master.color.createIndex(hue3, 0, 1), idxn = master.color.createIndex(hue2, 0, 1);
                                if (master.color.isModifierEnabled()) {
                                    colortmp = master.color.interpolated(master.color.createIndex(hue, 0, 1), Math.max(idxp, idxn), normalized_count - (long) normalized_count);
                                } else {
                                    colortmp = master.color.interpolated(Math.min(idxp, idxn), master.color.createIndex(hue, 0, 1), normalized_count - (long) normalized_count);
                                }
                            }
                        }
                        master.getArgand().setPixel(i, j, colortmp);
                    } else {
                        master.getArgand().setPixel(i, j, partImage.pixelContainer.getPixel(i, j));
                    }
                }
            }
        }
    }
    @Override
    public boolean allComplete() {
        return (countCompletedThreads() == (nx * ny));
    }
    class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        ComplexFractalGenerator copyOfMaster;
        int startx, starty, endx, endy;
        public SlaveRunner(int index, int startx, int endx, int starty, int endy) {
            super(index);
            this.startx = startx;
            this.starty = starty;
            this.endx = endx;
            this.endy = endy;
            this.copyOfMaster = new ComplexFractalGenerator(new ComplexFractalParams(master.getParams()), master.getProgressPublisher());
        }
        @Override
        public synchronized void pause() throws InterruptedException {
            copyOfMaster.pause();
        }
        @Override
        public synchronized void resume() throws InterruptedException {
            copyOfMaster.resume();
        }
        @Override
        public void generate() {
            copyOfMaster.generate(startx, endx, starty, endy, (int) iterations, escape_radius, constant);
            onCompletion();
        }
        @Override
        public void onCompletion() {
            if (copyOfMaster.color.getMode() == Colors.MODE.HISTOGRAM || copyOfMaster.color.getMode() == Colors.MODE.RANK_ORDER) {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getEscapedata(), copyOfMaster.getNormalized_escapes(), copyOfMaster.getHistogram(), startx, endx, starty, endy);
            } else {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getEscapedata(), copyOfMaster.getNormalized_escapes(), new PixelContainer(copyOfMaster.getArgand()), startx, endx, starty, endy);
            }
            float completion = ((float) countCompletedThreads() / (nx * ny)) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion, index);
        }
    }
}