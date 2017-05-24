package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * Threaded IFS Fractal Generator
 */
public class ThreadedIFSGenerator extends ThreadedGenerator {
    private IFSGenerator master;
    private volatile PartIFSData[] data;
    public ThreadedIFSGenerator(IFSGenerator generator) {
        master = generator;
        threads = new ThreadedGenerator.SlaveRunner[master.getParams().getThreads()];
        data = new PartIFSData[threads.length];
    }
    @Override
    public int countCompletedThreads() {
        int ctr = 0;
        for (@Nullable PartIFSData partImage : data) {
            if (partImage != null) ctr++;
        }
        return ctr;
    }
    public void generate() {
        if (master.getParams().useThreadedGenerator()) {
            int idx = 0;
            for (int t = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; t < threads.length; ++t) {
                threads[t] = new SlaveRunner(idx);
                master.getProgressPublisher().publish("Initiated thread: " + (idx + 1), (float) idx / threads.length, idx);
                idx++;
                threads[t].start();
            }
            try {
                joinAll();
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
    private class SlaveRunner extends ThreadedGenerator.SlaveRunner {
        private IFSGenerator copyOfMaster;
        public SlaveRunner(int index) {
            super(index);
            int iterations;
            copyOfMaster = new IFSGenerator(new IFSFractalParams(master.getParams()), master.getProgressPublisher());
            if (index == data.length - 1) {
                iterations = copyOfMaster.getDepth() % threads.length;
            } else {
                iterations = copyOfMaster.getDepth() / threads.length;
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
            float completion = ((float) countCompletedThreads() / threads.length) * 100.0f;
            master.getProgressPublisher().publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion, index);
        }
    }
}