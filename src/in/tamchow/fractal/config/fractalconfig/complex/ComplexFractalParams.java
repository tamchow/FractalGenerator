package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;

import java.io.Serializable;
/**
 * Encapsulates @code ComplexFractalInitParams and @code ComplexFractalRunParams
 */
public class ComplexFractalParams implements Serializable {
    public ComplexFractalRunParams runParams;
    public ComplexFractalInitParams initParams;
    public ZoomConfig zoomConfig;
    public int x_threads, y_threads;
    public ComplexFractalParams() {
        runParams = new ComplexFractalRunParams(); initParams = new ComplexFractalInitParams(); x_threads = 1;
        y_threads = 1; zoomConfig=null;
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams, int x_threads, int y_threads) {
        this.initParams = new ComplexFractalInitParams(initParams);
        this.runParams = new ComplexFractalRunParams(runParams); this.x_threads = x_threads; this.y_threads = y_threads;
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams) {
        this.initParams = new ComplexFractalInitParams(initParams);
        this.runParams = new ComplexFractalRunParams(runParams); this.x_threads = 1; this.y_threads = 1;
    }
    public ComplexFractalParams(ComplexFractalParams params) {
        this.initParams = new ComplexFractalInitParams(params.initParams);
        this.runParams = new ComplexFractalRunParams(params.runParams); this.x_threads = params.x_threads;
        this.y_threads = params.y_threads; this.zoomConfig = new ZoomConfig(params.zoomConfig);
    }
    public boolean useThreadedGenerator() {return (x_threads > 1 || y_threads > 1);}
    public void threadDataFromString(String data) {
        x_threads = Integer.valueOf(data.split(",")[0]); y_threads = Integer.valueOf(data.split(",")[1]);
    }
    public void setZoomConfig(ZoomConfig config) {
        zoomConfig = new ZoomConfig(config);
    }
}
