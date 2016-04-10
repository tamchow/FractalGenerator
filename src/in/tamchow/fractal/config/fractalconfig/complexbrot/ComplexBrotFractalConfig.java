package in.tamchow.fractal.config.fractalconfig.complexbrot;
import in.tamchow.fractal.config.Config;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
/**
 * Holds batch parameters for Complex brot fractals
 */
public class ComplexBrotFractalConfig extends Config implements Serializable {
    ComplexBrotFractalParams[] params;
    public ComplexBrotFractalConfig(int transtime, int fps, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
    }
    public ComplexBrotFractalConfig(@NotNull ComplexBrotFractalConfig config) {
        setWait(config.getWait());
        setFps(config.getFps());
        setTranstime(config.transtime);
        setParams(config.getParams());
    }
    public ComplexBrotFractalConfig(int transtime, int fps, int wait, @NotNull ComplexBrotFractalParams[] config) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        setParams(config);
    }
    public ComplexBrotFractalParams[] getParams() {
        return params;
    }
    public void setParams(@NotNull ComplexBrotFractalParams[] config) {
        this.params = new ComplexBrotFractalParams[config.length];
        for (int i = 0; i < config.length; i++) {
            params[i] = new ComplexBrotFractalParams(config[i]);
        }
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String representation = "[ComplexBrotFractalConfig]" + "\n" + "[Globals]\n" + transtime + "\n" + fps + "\n" + wait + "\n[EndGlobals]\n[Fractals]";
        for (@NotNull ComplexBrotFractalParams param : params) {
            representation += "\n" + param.getPath();
        }
        representation += "\n[EndFractals]";
        return representation + "\n";
    }
}