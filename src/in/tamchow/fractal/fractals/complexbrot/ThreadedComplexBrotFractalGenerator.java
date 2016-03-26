package in.tamchow.fractal.fractals.complexbrot;

import in.tamchow.fractal.fractals.ThreadedGenerator;

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
        for (PartComplexBrotFractalData partImage : data) {
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
        for (int i = 0; i < threads; i++) {
            SlaveRunner runner = new SlaveRunner(idx);
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
            for (PartComplexBrotFractalData part : data) {
                for (int i = 0; i < master.bases.length; i++) {
                    master.bases[i] = addDDA(master.bases[i], part.bases[i]);
                }
            }
            master.createImage();
        } catch (Exception e) {
            e.printStackTrace();
            //master.getProgressPublisher().println("Exception:" + e.getMessage());
        }
    }

    private int[][] addDDA(int[][] a, int[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("Dimensions of both arguments must be the same.");
        }
        int[][] c = new int[a.length][a[0].length];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c[i].length; j++) {
                c[i][j] = a[i][j] + b[i][j];
            }
        }
        return c;
    }

    class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        int index;
        ComplexBrotFractalGenerator copyOfMaster;

        public SlaveRunner(int index) {
            super(index);
            long num_points;
            this.copyOfMaster = new ComplexBrotFractalGenerator(master.getParams(), master.getProgressPublisher());
            if (index == data.length - 1) {
                num_points = copyOfMaster.getParams().getNum_points() % threads;
            } else {
                num_points = copyOfMaster.getParams().getNum_points() / threads;
            }
            copyOfMaster.setDepth(num_points);
        }

        @Override
        public void run() {
            copyOfMaster.generate();
        }

        @Override
        public void onCompletion() {
            data[index] = new PartComplexBrotFractalData(copyOfMaster.getBases());
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion);
        }
    }
}