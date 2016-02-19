package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.fractals.ThreadedGenerator;

import java.io.Serializable;
/**
 * Multihreaded Complex Brot Fractal generator
 * TODO: Implement
 */
public class ThreadedComplexBrotFractalGenerator extends ThreadedGenerator implements Serializable {
    ComplexBrotFractalGenerator master;
    PartComplexBrotFractalData[] data;
    int threads;
    public ThreadedComplexBrotFractalGenerator(ComplexBrotFractalGenerator generator) {
        master = generator; threads = master.getParams().getNum_threads();
    }
    class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        int index;
        ComplexBrotFractalGenerator copyOfMaster;
        public SlaveRunner(int index) {
            this.index = index; long num_points;
            this.copyOfMaster = new ComplexBrotFractalGenerator(master.getParams(), master.getProgressPublisher());
            if (index == data.length - 1) {
                num_points = copyOfMaster.getParams().getNum_points() % threads;
            } else {
                num_points = copyOfMaster.getParams().getNum_points() / threads;
            } copyOfMaster.getParams().setNum_points(num_points);
        }
        @Override
        public void run() {
        }
        @Override
        public void onCompletion() {
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion);
        }
    }
    @Override
    public int countCompletedThreads() {
        int ctr = 0; for (PartComplexBrotFractalData partImage : data) {if (partImage != null) ctr++;} return ctr;
    }
    @Override
    public boolean allComplete() {return (countCompletedThreads() == threads);}
    public void generate() {
        int idx = 0; for (int i = 0; i < threads; i++) {
            SlaveRunner runner = new SlaveRunner(idx);
            master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), idx); idx++; runner.start();
        } try {
            synchronized (lock) {
                while (!allComplete()) {lock.wait(1000);} lock.notifyAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //master.getProgressPublisher().println("Exception:" + e.getMessage());
        }
    }

}