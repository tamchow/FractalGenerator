package in.tamchow.fractal.imgutils;

/**
 * Superclass for set configurations
 */
public class Config {
    int fps, transtime, wait;

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getTranstime() {
        return transtime;
    }

    public void setTranstime(int transtime) {
        this.transtime = transtime;
    }

    public void configFromStrings(String[] config) {
        fps = Integer.valueOf(config[0]);
        transtime = Integer.valueOf(config[1]);
        wait = Integer.valueOf(config[2]);
    }
}
