package in.tamchow.fractal.config.fractalconfig;
import in.tamchow.fractal.config.Config;
/**
 * Configuration for the fractal
 */
public class FractalConfig extends Config {
    FractalParams[] params;
    public FractalConfig(int transtime, int fps, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
    }
    public FractalConfig(FractalConfig config) {
        setWait(config.getWait());
        setFps(config.getFps());
        setTranstime(config.transtime);
        setParams(config.getParams());
    }
    public FractalParams[] getParams() {
        return params;
    }
    public void setParams(FractalParams[] config) {
        this.params = new FractalParams[config.length];
        for (int i = 0; i < config.length; i++) {
            params[i] = new FractalParams(config[i]);
        }
    }
    public FractalConfig(int transtime, int fps, int wait, FractalParams[] config) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        setParams(config);
    }
    public void readConfig(FractalParams[] config) {
        setParams(config);
    }
}