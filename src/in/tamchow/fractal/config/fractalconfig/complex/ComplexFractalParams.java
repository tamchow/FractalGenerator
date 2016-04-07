package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import java.io.Serializable;
/**
 * Encapsulates @code ComplexFractalInitParams and @code ComplexFractalRunParams
 */
public class ComplexFractalParams implements Serializable {
    public ComplexFractalRunParams runParams;
    public ComplexFractalInitParams initParams;
    public ZoomConfig zoomConfig = new ZoomConfig();
    public int x_threads, y_threads;
    public PixelContainer.PostProcessMode postprocessMode;
    public String path;
    public ComplexFractalParams() {
        runParams = new ComplexFractalRunParams();
        initParams = new ComplexFractalInitParams();
        x_threads = 1;
        y_threads = 1;
        zoomConfig = null;
        setPostprocessMode(PixelContainer.PostProcessMode.NONE);
        setPath("");
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams, int x_threads, int y_threads) {
        this.initParams = new ComplexFractalInitParams(initParams);
        setPath("");
        this.runParams = new ComplexFractalRunParams(runParams);
        this.x_threads = x_threads;
        this.y_threads = y_threads;
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams) {
        this.initParams = new ComplexFractalInitParams(initParams);
        if (runParams != null) {
            this.runParams = new ComplexFractalRunParams(runParams);
        }
        this.x_threads = 1;
        this.y_threads = 1;
        setPostprocessMode(PixelContainer.PostProcessMode.NONE);
        setPath("");
    }
    public ComplexFractalParams(ComplexFractalParams params) {
        this.initParams = new ComplexFractalInitParams(params.initParams);
        this.runParams = new ComplexFractalRunParams(params.runParams);
        this.x_threads = params.x_threads;
        this.y_threads = params.y_threads;
        if (params.zoomConfig.zooms != null) {
            this.zoomConfig = new ZoomConfig(params.zoomConfig);
        }
        setPostprocessMode(params.getPostprocessMode());
        setPath(params.getPath());
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public PixelContainer.PostProcessMode getPostprocessMode() {
        return postprocessMode;
    }
    public void setPostprocessMode(PixelContainer.PostProcessMode postprocessMode) {
        this.postprocessMode = postprocessMode;
    }
    public boolean useThreadedGenerator() {
        return (x_threads > 1 || y_threads > 1);
    }
    public void threadDataFromString(String data) {
        String[] parts = StringManipulator.split(data, " ");
        x_threads = MathUtils.clamp(Integer.valueOf(parts[0]), 1, initParams.getWidth());
        y_threads = MathUtils.clamp(Integer.valueOf(parts[1]), 1, initParams.getHeight());
    }
    public void setZoomConfig(ZoomConfig config) {
        zoomConfig = new ZoomConfig(config);
    }
    @Override
    public String toString() {
        return "Threads:" + x_threads + "," + y_threads + "\nPostprocessing:" + postprocessMode + "\n" + ((zoomConfig != null) ? (zoomConfig + "\n") : "") + initParams + "\n" + runParams + "\n";
    }
}