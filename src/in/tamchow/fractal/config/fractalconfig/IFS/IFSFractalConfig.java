package in.tamchow.fractal.config.fractalconfig.IFS;
import in.tamchow.fractal.config.Config;
/**
 * Holds configuration for an IFS fractal
 */
public class IFSFractalConfig extends Config {
    IFSFractalParams[] params;
    public IFSFractalConfig(int transtime, int fps, int wait) {setFps(fps); setTranstime(transtime); setWait(wait);}
    public IFSFractalConfig(IFSFractalConfig config) {
        setWait(config.getWait()); setFps(config.getFps()); setTranstime(config.transtime);
        setParams(config.getParams());
    }
    public IFSFractalParams[] getParams() {
        return params;
    }
    public void setParams(IFSFractalParams[] config) {
        this.params = new IFSFractalParams[config.length]; for (int i = 0; i < config.length; i++) {
            params[i] = new IFSFractalParams(config[i]);
        }
    }
    public IFSFractalConfig(int transtime, int fps, int wait, IFSFractalParams[] config) {
        setFps(fps); setTranstime(transtime); setWait(wait); setParams(config);
    }
    @Override
    public String toString() {
        String representation = "[IFSFractalConfig]" + "\n" + "[Globals]\n" + transtime + "\n" + fps + "\n" + wait + "\n[EndGlobals]\n[Fractals]";
        for (IFSFractalParams param : params) {representation += "\n" + param.getPath();}
        representation += "\n[EndFractals]"; return representation + "\n";
    }
}