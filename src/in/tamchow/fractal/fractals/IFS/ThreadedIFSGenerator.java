package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.fractals.ThreadedGenerator;
/**
 * Threaded IFS Fractal Generator
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
        for (PartIFSData partImage : data) {
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
                for (PartIFSData partIFSData : data) {
                    master.getPlane().add(partIFSData.getPartPlane());
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
        public void run() {
            if (copyOfMaster.getParams().getFrameskip() > 0) {
                throw new UnsupportedOperationException("Animations cannot be generated in multithreaded mode,\n" + "Due to risk of corrupted output.");
            } else {
                copyOfMaster.generate();
            }
        }
        @Override
        public void onCompletion() {
            data[index] = new PartIFSData(copyOfMaster.getPlane());
            float completion = ((float) countCompletedThreads() / threads) * 100.0f;
            master.progressPublisher.publish("Thread " + (index + 1) + " has completed, total completion = " + completion + "%", completion);
        }
    }
}