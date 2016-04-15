package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.Color_Utils_Config;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
/**
 * Multithreading for the fractal generator
 */
public final class ThreadedComplexFractalGenerator extends ThreadedGenerator implements Serializable {
    final ComplexFractalGenerator master;
    PartComplexFractalData[] buffer;
    long iterations;
    double escape_radius;
    @Nullable
    Complex constant;
    int nx, ny;
    public ThreadedComplexFractalGenerator(int x_threads, int y_threads, ComplexFractalGenerator master, int iterations, double escape_radius, Complex constant) {
        this.master = master;
        this.iterations = iterations;
        this.escape_radius = escape_radius;
        this.constant = constant;
        nx = x_threads;
        ny = y_threads;
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
        generate(0, master.argand.getWidth(), 0, master.argand.getHeight());
    }
    public void generate(int startx, int endx, int starty, int endy) {
        int idx = 0;
        for (int t = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; t < nx * ny; ++t) {
            @NotNull int[] coords = master.start_end_coordinates(startx, endx, starty, endy, nx, t % nx, ny, t / nx);
            @NotNull SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1], coords[2], coords[3]);
            master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), (float) idx / (nx * ny), idx);
            idx++;
            runner.start();
        }
        wrapUp();
    }
    @Override
    public void finalizeGeneration() {
        @NotNull int[] histogram = new int[(int) iterations + 2];
        int total = 0;
        if (master.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || master.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
            for (@NotNull PartComplexFractalData partImage : buffer) {
                for (int i = 0; i < histogram.length; i++) {
                    histogram[i] += partImage.histogram[i];
                }
            }
        }
        for (int i = 0; i < iterations; i++) {
            total += histogram[i];
        }
        if (master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
            System.arraycopy(MathUtils.rankListFromHistogram(histogram), 0, histogram, 0, histogram.length);
        }
        double scaling = Math.pow(master.zoom, master.zoom_factor);
        for (@NotNull PartComplexFractalData partImage : buffer) {
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
                    if (master.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || master.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                        if (master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                            if (master.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR) {
                                int color1 = master.color.getColor(master.color.createIndex(((double) MathUtils.indexOf(histogram, ep)) / iterations, 0, 1, scaling)), color2 = master.color.getColor(master.color.createIndex(((double) MathUtils.indexOf(histogram, e)) / iterations, 0, 1, scaling)), color3 = master.color.getColor(master.color.createIndex(((double) MathUtils.indexOf(histogram, en)) / iterations, 0, 1, scaling));
                                int colortmp1 = Color_Utils_Config.linearInterpolated(color1, color2, normalized_count - (long) normalized_count, master.color.getByParts());
                                int colortmp2 = Color_Utils_Config.linearInterpolated(color2, color3, normalized_count - (long) normalized_count, master.color.getByParts());
                                if (master.color.isLogIndex()) {
                                    colortmp = Color_Utils_Config.linearInterpolated(colortmp1, colortmp2, normalized_count - (long) normalized_count, master.color.getByParts());
                                } else {
                                    colortmp = color2;
                                }
                            } else {
                                int idxp = master.color.createIndex(((double) MathUtils.indexOf(histogram, ep)) / iterations, 0, 1, scaling),
                                        idxn = master.color.createIndex(((double) MathUtils.indexOf(histogram, en)) / iterations, 0, 1, scaling), idxt = Math.min(idxp, idxn);
                                colortmp = master.color.splineInterpolated(master.color.createIndex(((double) MathUtils.indexOf(histogram, e)) / iterations, 0, 1, scaling), idxt, normalized_count - (long) normalized_count);
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
                            if (master.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR) {
                                int colortmp1 = Color_Utils_Config.linearInterpolated(master.color.getColor(master.color.createIndex(hue, 0, 1, scaling)), master.color.getColor(master.color.createIndex(hue2, 0, 1, scaling)), normalized_count - (long) normalized_count, master.color.getByParts());
                                int colortmp2 = Color_Utils_Config.linearInterpolated(master.color.getColor(master.color.createIndex(hue3, 0, 1, scaling)), master.color.getColor(master.color.createIndex(hue, 0, 1, scaling)), normalized_count - (long) normalized_count, master.color.getByParts());
                                colortmp = Color_Utils_Config.linearInterpolated(colortmp2, colortmp1, normalized_count - (long) normalized_count, master.color.getByParts());
                            } else {
                                int idxp = master.color.createIndex(hue3, 0, 1, scaling),
                                        idxn = master.color.createIndex(hue2, 0, 1, scaling), idxt = Math.min(idxp, idxn);
                                colortmp = master.color.splineInterpolated(master.color.createIndex(hue, 0, 1, scaling), idxt, normalized_count - (long) normalized_count);
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
        public void generate() {
            copyOfMaster.generate(startx, endx, starty, endy, (int) iterations, escape_radius, constant);
            onCompletion();
        }
        @Override
        public void onCompleted() {
            if (copyOfMaster.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM || copyOfMaster.color.getMode() == Colors.CALCULATIONS.COLOR_HISTOGRAM_LINEAR || copyOfMaster.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_LINEAR || copyOfMaster.color.getMode() == Colors.CALCULATIONS.RANK_ORDER_SPLINE) {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getEscapedata(), copyOfMaster.getNormalized_escapes(), copyOfMaster.getHistogram(), startx, endx, starty, endy);
            } else {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getEscapedata(), copyOfMaster.getNormalized_escapes(), new PixelContainer(copyOfMaster.getArgand()), startx, endx, starty, endy);
            }
            float completion = ((float) countCompletedThreads() / (nx * ny)) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion, index);
        }
    }
}