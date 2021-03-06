package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;

import static in.tamchow.fractal.color.Colors.MODE.*;
import static in.tamchow.fractal.helpers.math.MathUtils.*;
/**
 * Multithreading for the fractal generator
 */
public final class ThreadedComplexFractalGenerator extends ThreadedGenerator {
    private final ComplexFractalGenerator master;
    private volatile PartComplexFractalData[] buffer;
    private long iterations;
    private double escapeRadius;
    @Nullable
    private Complex constant;
    private int nx, ny;
    public ThreadedComplexFractalGenerator(int xThreads, int yThreads, ComplexFractalGenerator master, int iterations, double escapeRadius, Complex constant) {
        this.master = master;
        this.iterations = iterations;
        this.escapeRadius = escapeRadius;
        this.constant = constant;
        nx = xThreads;
        ny = yThreads;
        threads = new ThreadedGenerator.SlaveRunner[nx * ny];
        buffer = new PartComplexFractalData[threads.length];
    }
    public ThreadedComplexFractalGenerator(@NotNull ComplexFractalGenerator master) {
        this(master, master.getParams());
    }
    public ThreadedComplexFractalGenerator(ComplexFractalGenerator master, @NotNull ComplexFractalParams config) {
        this(config.getXThreads(), config.getYThreads(), master,
                config.runParams.iterations, config.runParams.escapeRadius, config.runParams.constant);
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
    public void generate(int startX, int endX, int startY, int endY) {
        int idx = 0;
        for (int t = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; t < threads.length; ++t) {
            @NotNull int[] coords = ComplexFractalGenerator.start_end_coordinates(startX, endX, startY, endY, nx, t % nx, ny, t / nx);
            threads[t] = new SlaveRunner(idx, coords[0], coords[1], coords[2], coords[3]);
            master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), (float) idx / threads.length, idx);
            idx++;
            threads[t].start();
        }
        try {
            joinAll();
        } catch (InterruptedException interrupted) {
            interrupted.printStackTrace();
        }
    }
    @Override
    public void finalizeGeneration() {
        @NotNull int[] histogram = new int[(int) iterations + 1];
        int total = 0;
        if (isAnyOf(master.getColor().getMode(), HISTOGRAM, RANK_ORDER)) {
            for (@NotNull PartComplexFractalData partImage : buffer) {
                for (int i = 0; i < histogram.length; i++) {
                    histogram[i] += partImage.histogram[i];
                }
            }
            for (int i = 0; i < iterations; i++) {
                total += histogram[i];
            }
            if (master.color.getMode() == RANK_ORDER) {
                System.arraycopy(rankListFromHistogram(histogram), 0, histogram, 0, histogram.length);
            }
        }
        //double scaling = master.basePrecision * Math.pow(master.zoom, master.zoom_factor);
        for (@Nullable PartComplexFractalData partImage : buffer) {
            if (partImage == null) {
                continue;
            }
            for (int i = partImage.starty; i < partImage.endy; i++) {
                for (int j = partImage.startx; j < partImage.endx; j++) {
                    master.orbitEscapeData[i][j] = partImage.escapedata[i][j];
                    master.normalizedEscapes[i][j] = partImage.normalized_escapes[i][j];
                    if (isAnyOf(master.getColor().getMode(), CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
                        master.miscellaneous[i][j] = partImage.miscellaneous[i][j];
                    }
                    if (isAnyOf(master.getColor().getMode(), HISTOGRAM, RANK_ORDER)) {
                        double normalized_count = master.normalizedEscapes[i][j];
                        int colortmp, pi = i, pj = j - 1, ni = i, nj = j + 1;
                        if (pj < 0) {
                            pi = (i == 0) ? i : i - 1;
                            pj = master.orbitEscapeData[pi].length - 1;
                        }
                        if (nj >= master.orbitEscapeData[i].length) {
                            ni = (i == master.orbitEscapeData.length - 1) ? i : i + 1;
                            nj = 0;
                        }
                        int ep = master.orbitEscapeData[pi][pj], en = master.orbitEscapeData[ni][nj], e = master.orbitEscapeData[i][j];
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
                            switch (master.color.getInterpolationType()) {
                                case LINEAR:
                                    colortmp = master.color.interpolated(idxp, idx, idxn,
                                            normalized_count - (long) normalized_count);
                                    break;
                                case CATMULL_ROM_SPLINE: {
                                    if (master.color.isModifierEnabled()) {
                                        colortmp = master.color.interpolated(idx, idxMin, idxMax,
                                                normalized_count - (long) normalized_count);
                                    } else {
                                        if (master.color.isLogIndex()) {
                                            colortmp = master.color.interpolated(idxMin, idx,
                                                    normalized_count - (long) normalized_count);
                                        } else {
                                            colortmp = master.color.interpolated(idx, idxMax,
                                                    normalized_count - (long) normalized_count);
                                        }
                                    }
                                }
                                break;
                                default:
                                    // The other option is only applicable during palette generation, if it appears here, it's an error
                                    colortmp = Integer.MIN_VALUE;
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
                            switch (master.color.getInterpolationType()) {
                                case LINEAR:
                                    colortmp = master.color.interpolated(master.color.createIndex(hue2, 0, 1),
                                            master.color.createIndex(hue, 0, 1),
                                            master.color.createIndex(hue3, 0, 1), normalized_count - (long) normalized_count);
                                    break;
                                case CATMULL_ROM_SPLINE:
                                    int idxp = master.color.createIndex(hue3, 0, 1), idxn = master.color.createIndex(hue2, 0, 1);
                                    if (master.color.isModifierEnabled()) {
                                        colortmp = master.color.interpolated(master.color.createIndex(hue, 0, 1), Math.max(idxp, idxn),
                                                normalized_count - (long) normalized_count);
                                    } else {
                                        colortmp = master.color.interpolated(Math.min(idxp, idxn), master.color.createIndex(hue, 0, 1),
                                                normalized_count - (long) normalized_count);
                                    }
                                    break;
                                default:
                                    // The other option is only applicable during palette generation, if it appears here, it's an error
                                    colortmp = Integer.MIN_VALUE;
                            }
                        }
                        master.getArgand().setPixel(i, j, colortmp);
                    } else if (!isAnyOf(master.getColor().getMode(), CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
                        master.getArgand().setPixel(i, j,
                                partImage.pixelContainer.getPixel(i, j));
                    }
                }
            }
        }
        if (isAnyOf(master.getColor().getMode(), CUMULATIVE_ANGLE, CUMULATIVE_DISTANCE)) {
            master.colorizeWRTDistanceOrAngle();
        }
    }
    private class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        private ComplexFractalGenerator copyOfMaster;
        private int startX, startY, endX, endY;
        public SlaveRunner(int index, int startX, int endX, int startY, int endY) {
            super(index);
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
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
            copyOfMaster.generate(startX, endX, startY, endY, (int) iterations, escapeRadius, constant);
            onCompletion();
        }
        @Override
        public void onCompletion() {
            if (isAnyOf(copyOfMaster.getColor().getMode(), Colors.MODE.HISTOGRAM, Colors.MODE.RANK_ORDER)) {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getOrbitEscapeData(), copyOfMaster.getNormalizedEscapes(), copyOfMaster.getHistogram(), startX, endX, startY, endY);
            } else if (isAnyOf(copyOfMaster.getColor().getMode(), Colors.MODE.CUMULATIVE_DISTANCE, Colors.MODE.CUMULATIVE_ANGLE)) {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getOrbitEscapeData(), copyOfMaster.getNormalizedEscapes(), copyOfMaster.getMiscellaneous(), startX, endX, startY, endY);
            } else {
                buffer[index] = new PartComplexFractalData(copyOfMaster.getOrbitEscapeData(), copyOfMaster.getNormalizedEscapes(), new PixelContainer(copyOfMaster.getArgand()), startX, endX, startY, endY);
            }
            float completion = ((float) countCompletedThreads() / (nx * ny)) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion, index);
        }
    }
}