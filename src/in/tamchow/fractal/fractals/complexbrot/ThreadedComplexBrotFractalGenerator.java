package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.fractals.ThreadedGenerator;
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
    int threads;
    public ThreadedComplexBrotFractalGenerator(ComplexBrotFractalGenerator generator) {
        master = generator;
        threads = master.getParams().getNum_threads();
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
    public void generate() {
        int idx = 0;
        for (int i = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; i < threads; i++) {
            @NotNull int[] coords = master.start_end_coordinates(i, threads);
            @NotNull SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1]);
            master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), idx);
            idx++;
            runner.start();
        }
        try {
            synchronized (lock) {
                while (!allComplete()) {
                    lock.wait(1000);
                }
                lock.notifyAll();
            }
            for (@NotNull PartComplexBrotFractalData part : data) {
                for (int i = 0; i < master.bases.length; ++i) {
                    master.bases[i] = MathUtils.intDDAAdd(master.bases[i], part.getBases()[i]);
                }
            }
            master.createImage();
        } catch (Exception e) {
            e.printStackTrace();
            //master.getProgressPublisher().println("Exception:" + e.getMessage());
        }
    }
    class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        ComplexBrotFractalGenerator copyOfMaster;
        private int start, end;
        public SlaveRunner(int index, int start, int end) {
            super(index);
            this.copyOfMaster = new ComplexBrotFractalGenerator(master.getParams(), master.getProgressPublisher());
            this.start = start;
            this.end = end;
        }
        @Override
        public void generate() {
            copyOfMaster.generate(start, end);
            onCompletion();
        }
        @Override
        public void onCompleted() {
            data[index] = new PartComplexBrotFractalData(copyOfMaster.getBases());
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion);
        }
    }
}