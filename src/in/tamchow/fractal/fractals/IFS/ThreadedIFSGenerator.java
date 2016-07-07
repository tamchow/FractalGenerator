package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * Threaded IFS Fractal Generator
 */
public class ThreadedIFSGenerator extends ThreadedGenerator {
    private IFSGenerator master;
    private volatile PartIFSData[] data;
    private int threads;
    public ThreadedIFSGenerator(IFSGenerator generator) {
        master = generator;
        threads = master.getParams().getThreads();
        data = new PartIFSData[threads];
    }
    @Override
    public int countCompletedThreads() {
        int ctr = 0;
        for (@Nullable PartIFSData partImage : data) {
            if (partImage != null) ctr++;
        }
        return ctr;
    }
    @Override
    public boolean allComplete() {
        return (countCompletedThreads() == threads);
    }
    public void generate() {
        if (master.getParams().useThreadedGenerator()) {
            int idx = 0;
            for (int i = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; i < threads; i++) {
                @NotNull SlaveRunner runner = new SlaveRunner(idx);
                master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), (float) idx / threads, idx);
                idx++;
                runner.start();
            }
            try {
                wrapUp();
            } catch (InterruptedException interrupted) {
                interrupted.printStackTrace();
            }
        } else {
            master.generate();
        }
    }
    @Override
    public void finalizeGeneration() {
        for (@Nullable PartIFSData partIFSData : data) {
            if (partIFSData == null) {
                continue;
            }
            master.getPlane().add(partIFSData.getPartPlane(), true, partIFSData.getPartWeightData());
            master.getAnimation().addFrames(partIFSData.getPartAnimation());
        }
    }
    class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        int index;
        IFSGenerator copyOfMaster;
        public SlaveRunner(int index) {
            super(index);
            int iterations;
            copyOfMaster = new IFSGenerator(new IFSFractalParams(master.getParams()), master.getProgressPublisher());
            if (index == data.length - 1) {
                iterations = copyOfMaster.getDepth() % threads;
            } else {
                iterations = copyOfMaster.getDepth() / threads;
            }
            copyOfMaster.setDepth(iterations);
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
            copyOfMaster.generate(index);
            onCompletion();
        }
        @Override
        public void onCompletion() {
            data[index] = new PartIFSData(copyOfMaster.getPlane(), copyOfMaster.getAnimation(), copyOfMaster.getWeightDistribution());
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.getProgressPublisher().publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion, index);
        }
    }
}