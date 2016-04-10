package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
/**
 * Configuration for the fractal
 */
public class ComplexFractalConfig extends Config implements Serializable {
    ComplexFractalParams[] params;
    public ComplexFractalConfig(int transtime, int fps, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
    }
    public ComplexFractalConfig(@NotNull ComplexFractalConfig config) {
        setWait(config.getWait());
        setFps(config.getFps());
        setTranstime(config.transtime);
        setParams(config.getParams());
    }
    public ComplexFractalConfig(int transtime, int fps, int wait, @NotNull ComplexFractalParams[] config) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        setParams(config);
    }
    public ComplexFractalParams[] getParams() {
        return params;
    }
    public void setParams(@NotNull ComplexFractalParams[] config) {
        this.params = new ComplexFractalParams[config.length];
        for (int i = 0; i < config.length; i++) {
            params[i] = new ComplexFractalParams(config[i]);
        }
    }
    @Override
    public String toString() {
        String representation = String.format("[ComplexFractalConfig]%n[Globals]%n%d%n%d$n%d%n[EndGlobals]%n[Fractals]",
                transtime, fps, wait);
        for (@NotNull ComplexFractalParams param : params) {
            representation += "\n" + param.getPath();
        }
        representation += "\n[EndFractals]";
        return representation;
    }
}