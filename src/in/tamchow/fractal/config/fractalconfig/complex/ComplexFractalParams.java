package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.imgutils.ImageData;

import java.io.Serializable;
/**
 * Encapsulates @code ComplexFractalInitParams and @code ComplexFractalRunParams
 */
public class ComplexFractalParams implements Serializable {
    public ComplexFractalRunParams runParams;
    public ComplexFractalInitParams initParams;
    public ZoomConfig zoomConfig;
    public int x_threads, y_threads;
    public ImageData.PostProcessMode postprocessMode;
    public ComplexFractalParams() {
        runParams = new ComplexFractalRunParams(); initParams = new ComplexFractalInitParams(); x_threads = 1;
        y_threads = 1; zoomConfig = null; setPostprocessMode(ImageData.PostProcessMode.NONE);
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams, int x_threads, int y_threads) {
        this.initParams = new ComplexFractalInitParams(initParams);
        this.runParams = new ComplexFractalRunParams(runParams); this.x_threads = x_threads; this.y_threads = y_threads;
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams) {
        this.initParams = new ComplexFractalInitParams(initParams);
        if (runParams != null) {this.runParams = new ComplexFractalRunParams(runParams);} this.x_threads = 1;
        this.y_threads = 1; setPostprocessMode(ImageData.PostProcessMode.NONE);
    }
    public ComplexFractalParams(ComplexFractalParams params) {
        this.initParams = new ComplexFractalInitParams(params.initParams);
        this.runParams = new ComplexFractalRunParams(params.runParams); this.x_threads = params.x_threads;
        this.y_threads = params.y_threads; this.zoomConfig = new ZoomConfig(params.zoomConfig);
        setPostprocessMode(params.getPostprocessMode());
    }
    public ImageData.PostProcessMode getPostprocessMode() {return postprocessMode;}
    public void setPostprocessMode(ImageData.PostProcessMode postprocessMode) {this.postprocessMode = postprocessMode;}
    public boolean useThreadedGenerator() {return (x_threads > 1 || y_threads > 1);}
    public void threadDataFromString(String data) {
        x_threads = Integer.valueOf(data.split(",")[0]); y_threads = Integer.valueOf(data.split(" ")[1]);
    }
    public void setZoomConfig(ZoomConfig config) {
        zoomConfig = new ZoomConfig(config);
    }
}