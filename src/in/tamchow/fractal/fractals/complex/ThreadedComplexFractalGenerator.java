package in.tamchow.fractal.fractals.complex;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.complex.Complex;
/**
 * Multithreading for the fractal generator
 */
public class ThreadedComplexFractalGenerator {
    public final ComplexFractalGenerator master;
    boolean[] progress;PartImage[] buffer;
    long iterations;
    double escape_radius;
    Complex constant;
    int nx, ny;
    final Object lock;
    private int countCompletedThreads(){
        int ctr=0;for(boolean progression:progress){
            if(progression)ctr++;
        }return ctr;}
    public ThreadedComplexFractalGenerator(int x_threads, int y_threads, ComplexFractalGenerator master, int iterations, double escape_radius, Complex constant,Object lock) {
        this.master = master;
        this.iterations = iterations;
        this.escape_radius = escape_radius;
        this.constant = constant;
        nx = x_threads;
        ny = y_threads;
        progress = new boolean[nx * ny];
        buffer=new PartImage[progress.length];
        this.lock=lock;
    }
    public ThreadedComplexFractalGenerator(ComplexFractalGenerator master, ComplexFractalParams config,Object lock) {
        this.master = master;
        this.iterations = config.runParams.iterations;
        this.escape_radius = config.runParams.escape_radius;
        this.constant = config.runParams.constant; nx = config.x_threads; ny = config.y_threads;
        progress = new boolean[nx * ny];buffer=new PartImage[progress.length];
        this.lock=lock;
    }
    public void generate() {
        int idx = 0;
        for (int i = 0; i < ny; i++) {
            for (int j = 0; j < nx; j++) {
                int[] coords = master.start_end_coordinates(nx, j, ny, i);
                SlaveRunner runner = new SlaveRunner(idx, coords[0], coords[1], coords[2], coords[3]);
                master.getProgressPublisher().println("Initiated thread: " + idx); idx++;
                runner.start();}} try {synchronized (lock){
                while (!allComplete()){lock.wait(1000);}lock.notifyAll();
                for(PartImage partImage:buffer){
                    for(int i=partImage.starty;i<partImage.endy;i++){
                        for(int j=partImage.startx;j<partImage.endx;j++){
                            master.argand.setPixel(i,j,partImage.imageData.getPixel(i,j));
                        }
                    }
                }
        }
        } catch (Exception e) {master.getProgressPublisher().println("Exception:" + e.getMessage());}
    }
    public boolean allComplete() {
        for (boolean progression : progress) {
            if (!progression) {return false;}
        } return true;
    }
    class SlaveRunner extends Thread {
        ComplexFractalGenerator copyOfMaster;
        Thread executor;
        int index;
        int startx, starty, endx, endy;
        public SlaveRunner(int index, int startx, int endx,int starty, int endy) {
            this.index = index;
            this.startx = startx;
            this.starty = starty;
            this.endx = endx;
            this.endy = endy;
            this.copyOfMaster = new ComplexFractalGenerator(master.getParams(), master.getProgressPublisher());
        }
        public void start() {
            if (executor == null) {executor = new Thread(this);} executor.start();
        }
        public void run() {
            copyOfMaster.generate(startx, endx, starty, endy, (int) iterations, escape_radius, constant);
            onCompletion();
        }
        void onCompletion() {
            buffer[index]=new PartImage(new ImageData(copyOfMaster.getArgand()),startx,endx,starty,endy);
            progress[index] = true;float completion=((float)countCompletedThreads()/(nx*ny))*100.0f;
            master.progressPublisher.println("Thread " + index + " has completed, total completion = "+completion+"%");
        }
    }
}