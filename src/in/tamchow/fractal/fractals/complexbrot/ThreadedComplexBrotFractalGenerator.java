package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;

import java.io.Serializable;
/**
 * Multithreaded Complex Brot Fractal generator
 */
public class ThreadedComplexBrotFractalGenerator extends ThreadedGenerator implements Serializable {
    ComplexBrotFractalGenerator master;
    PartComplexBrotFractalData[] data;
    int threads, nx, ny;
    public ThreadedComplexBrotFractalGenerator(ComplexBrotFractalGenerator generator) {
        master = generator;
        if (master.isSequential()) {
            threads = nx * ny;
        } else {
            threads = master.getParams().getNum_threads();
        }
        data = new PartComplexBrotFractalData[threads];
    }
    @Override
    public int countCompletedThreads() {
        int ctr = 0;
        for (@Nullable PartComplexBrotFractalData partImage : data) {
            if (partImage != null) ctr++;
        }
        return ctr;
    }
    @Override
    public boolean allComplete() {
        return (countCompletedThreads() == threads);
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
    public void generate() {
        if (master.isSequential()) {
            generate(0, master.getImageWidth(), 0, master.getImageHeight());
        } else {
            int idx = 0;
            for (int i = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; i < threads; i++) {
                @NotNull int[] coords = master.start_end_coordinates(i, threads);
                @NotNull SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1]);
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
    }
    @Override
    public void finalizeGeneration() {
        for (@Nullable PartComplexBrotFractalData part : data) {
            if (part == null) {
                continue;
            }
            for (int i = 0; i < master.bases.length; ++i) {
                MathUtils.intDDAAdd(master.bases[i], part.getBases()[i]);
            }
            //master.setDiscardedPoints(master.getDiscardedPointsCount()+part.getDiscardedPoints());
        }
        master.createImage();
    }
    class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        ComplexBrotFractalGenerator copyOfMaster;
        private int start, end, startx, endx, starty, endy;
        public SlaveRunner(int index, int start, int end) {
            super(index);
            this.copyOfMaster = new ComplexBrotFractalGenerator(master.getParams(), master.getProgressPublisher());
            this.start = start;
            this.end = end;
        }
        public SlaveRunner(int index, int startx, int endx, int starty, int endy) {
            super(index);
            this.copyOfMaster = new ComplexBrotFractalGenerator(master.getParams(), master.getProgressPublisher());
            this.startx = startx;
            this.endx = endx;
            this.starty = starty;
            this.endy = endy;
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
            if (copyOfMaster.isSequential()) {
                copyOfMaster.generate(startx, endx, starty, endy);
            } else {
                copyOfMaster.generate(start, end);
            }
            onCompletion();
        }
        @Override
        public void onCompletion() {
            data[index] = new PartComplexBrotFractalData(copyOfMaster.getBases(), copyOfMaster.getDiscardedPointsCount());
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion, index);
        }
    }
}