package in.tamchow.fractal.config.fractalconfig.IFS;

import in.tamchow.fractal.config.Config;

import java.io.Serializable;

/**
 * Holds configuration for an IFS fractal
 */
public class IFSFractalConfig extends Config implements Serializable {
    IFSFractalParams[] params;

    public IFSFractalConfig(int transtime, int fps, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
    }

    public IFSFractalConfig(IFSFractalConfig config) {
        setWait(config.getWait());
        setFps(config.getFps());
        setTranstime(config.transtime);
        setParams(config.getParams());
    }

    public IFSFractalConfig(int transtime, int fps, int wait, IFSFractalParams[] config) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        setParams(config);
    }

    public IFSFractalParams[] getParams() {
        return params;
    }

    public void setParams(IFSFractalParams[] config) {
        this.params = new IFSFractalParams[config.length];
        for (int i = 0; i < config.length; i++) {
            params[i] = new IFSFractalParams(config[i]);
        }
    }

    @Override
    public String toString() {
        String representation = String.format("[IFSFractalConfig]%n[Globals]%n%d%n%d%n%d%nEndGlobals]%n[Fractals]",
                transtime, fps, wait);
        for (IFSFractalParams param : params) {
            representation += "\n" + param.getPath();
        }
        representation += "\n[EndFractals]";
        return representation + "\n";
    }
}