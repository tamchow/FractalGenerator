package in.tamchow.fractal.config.fractalconfig.l_system;
import in.tamchow.fractal.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
/**
 * Holds configuration for a L-System fractal
 */
public class LSFractalConfig extends Config implements Serializable {
    LSFractalParams[] params;
    public LSFractalConfig(int transtime, int fps, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
    }
    public LSFractalConfig(@NotNull LSFractalConfig config) {
        setWait(config.getWait());
        setFps(config.getFps());
        setTranstime(config.transtime);
        setParams(config.getParams());
    }
    public LSFractalConfig(int transtime, int fps, int wait, @NotNull LSFractalParams[] config) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        setParams(config);
    }
    public LSFractalParams[] getParams() {
        return params;
    }
    public void setParams(@NotNull LSFractalParams[] config) {
        this.params = new LSFractalParams[config.length];
        for (int i = 0; i < config.length; i++) {
            params[i] = new LSFractalParams(config[i]);
        }
    }
    @NotNull
    @Override
    public String toString() {
        String representation = String.format("[IFSFractalConfig]%n[Globals]%n%d%n%d%n%d%nEndGlobals]%n[Fractals]",
                transtime, fps, wait);
        for (@NotNull LSFractalParams param : params) {
            representation += "\n" + param.getPath();
        }
        representation += "\n[EndFractals]";
        return representation + "\n";
    }
}