package in.tamchow.fractal.config.fractalconfig.complexbrot;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.helpers.annotations.NotNull;

import java.io.Serializable;

import static in.tamchow.fractal.config.Strings.BLOCKS.*;
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
        String representation = String.format(COMPLEXBROT + "%n" + GLOBALS + "%n%d%n%d$n%d%n" + ENDGLOBALS + "%n" + FRACTALS,
                transtime, fps, wait);
        for (@NotNull ComplexBrotFractalParams param : params) {
            representation += "\n" + param.getPath();
        }
        representation += "\n" + ENDFRACTALS;
        return representation;
    }
}