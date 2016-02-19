package in.tamchow.fractal.config.fractalconfig.complexbrot;
import in.tamchow.fractal.config.Config;

import java.io.Serializable;
/**
 * Holds batch parameters for Complex brot fractals
 */
public class ComplexBrotFractalConfig extends Config implements Serializable {
    ComplexBrotFractalParams[] params;
    public ComplexBrotFractalConfig(int transtime, int fps, int wait) {setFps(fps); setTranstime(transtime); setWait(wait);}
    public ComplexBrotFractalConfig(ComplexBrotFractalConfig config) {
        setWait(config.getWait()); setFps(config.getFps()); setTranstime(config.transtime);
        setParams(config.getParams());
    }
    public ComplexBrotFractalParams[] getParams() {
        return params;
    }
    public void setParams(ComplexBrotFractalParams[] config) {
        this.params = new ComplexBrotFractalParams[config.length];
        for (int i = 0; i < config.length; i++) {params[i] = new ComplexBrotFractalParams(config[i]);}
    }
    public ComplexBrotFractalConfig(int transtime, int fps, int wait, ComplexBrotFractalParams[] config) {
        setFps(fps); setTranstime(transtime); setWait(wait); setParams(config);
    }
    @Override
    public String toString() {
        String representation = "[ComplexBrotFractalConfig]" + "\n" + "[Globals]\n" + transtime + "\n" + fps + "\n" + wait + "\n[EndGlobals]\n[Fractals]";
        for (ComplexBrotFractalParams param : params) {representation += "\n" + param.getPath();}
        representation += "\n[EndFractals]"; return representation + "\n";
    }
}