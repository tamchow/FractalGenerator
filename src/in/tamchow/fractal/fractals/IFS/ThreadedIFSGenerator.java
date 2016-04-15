package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * Threaded IFS Fractal Generator
 * <p/>
 * Note: May produce unpredictable results. Use not recommended.
 * <p/>
 * Expected result: Images with {@link IFSFractalParams#depth} times added colors.
 * <p/>
 * Debugging in progress.
 */
public class ThreadedIFSGenerator extends ThreadedGenerator {
    IFSGenerator master;
    PartIFSData[] data;
    int threads;
    public ThreadedIFSGenerator(IFSGenerator generator) {
        master = generator;
        threads = master.getParams().getThreads();
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
        int idx = 0;
        for (int i = (currentlyCompletedThreads == 0) ? 0 : currentlyCompletedThreads + 1; i < threads; i++) {
            @NotNull SlaveRunner runner = new SlaveRunner(idx);
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
                for (@NotNull PartIFSData partIFSData : data) {
                    master.getPlane().add(partIFSData.getPartPlane(), true, partIFSData.getPartWeightData());
                    master.getAnimation().addFrames(partIFSData.getPartAnimation());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //master.getProgressPublisher().println("Exception:" + e.getMessage());
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
        public void generate() {
            copyOfMaster.generate(index);
            onCompletion();
        }
        @Override
        public void onCompleted() {
            data[index] = new PartIFSData(copyOfMaster.getPlane(), copyOfMaster.getAnimation(), copyOfMaster.getWeightDistribution());
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion);
        }
    }
}