package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.Strings;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import static in.tamchow.fractal.config.Strings.DECLARATIONS.POSTPROCESSING;
import static in.tamchow.fractal.config.Strings.DECLARATIONS.THREADS;
/**
 * Encapsulates {@code ComplexFractalInitParams} and {@code ComplexFractalRunParams}
 */
public class ComplexFractalParams extends Config {
    @Nullable
    public ComplexFractalRunParams runParams;
    @Nullable
    public ComplexFractalInitParams initParams;
    @NotNull
    public ZoomConfig zoomConfig = new ZoomConfig();
    public PixelContainer.PostProcessMode postProcessMode;
    private int xThreads, yThreads;
    {
        setName(Strings.BLOCKS.COMPLEX);
    }
    public ComplexFractalParams() {
        super();
        runParams = new ComplexFractalRunParams();
        initParams = new ComplexFractalInitParams();
        initParams.setHeight(1);
        initParams.setWidth(1);
        setPostProcessMode(PixelContainer.PostProcessMode.NONE);
        setXThreads(1);
        setYThreads(1);
        setWait(0);
    }
    public ComplexFractalParams(@Nullable ComplexFractalInitParams initParams, @Nullable ComplexFractalRunParams runParams, int xThreads, int yThreads) {
        super();
        if (initParams != null) {
            this.initParams = new ComplexFractalInitParams(initParams);
        } else {
            this.initParams = new ComplexFractalInitParams();
            this.initParams.setHeight(1);
            this.initParams.setWidth(1);
        }
        if (runParams != null) {
            this.runParams = new ComplexFractalRunParams(runParams);
        } else {
            this.runParams = new ComplexFractalRunParams();
        }
        setPostProcessMode(PixelContainer.PostProcessMode.NONE);
        setHeight(this.initParams.getHeight());
        setWidth(this.initParams.getWidth());
        setXThreads(xThreads);
        setYThreads(yThreads);
        setWait(0);
    }
    public ComplexFractalParams(@Nullable ComplexFractalInitParams initParams, @Nullable ComplexFractalRunParams runParams) {
        if (initParams != null) {
            this.initParams = new ComplexFractalInitParams(initParams);
        } else {
            this.initParams = new ComplexFractalInitParams();
            this.initParams.setHeight(1);
            this.initParams.setWidth(1);
        }
        if (runParams != null) {
            this.runParams = new ComplexFractalRunParams(runParams);
        } else {
            this.runParams = new ComplexFractalRunParams();
        }
        setPath("");
        setPostProcessMode(PixelContainer.PostProcessMode.NONE);
        setXThreads(1);
        setYThreads(1);
        setWait(0);
    }
    public ComplexFractalParams(@NotNull ComplexFractalParams params) {
        this.initParams = new ComplexFractalInitParams(params.initParams);
        this.runParams = new ComplexFractalRunParams(params.runParams);
        setXThreads(params.getXThreads());
        setYThreads(params.getYThreads());
        if (params.zoomConfig.hasZooms()) {
            this.zoomConfig = new ZoomConfig(params.zoomConfig);
        }
        setHeight(initParams.getHeight());
        setWidth(initParams.getWidth());
        setPostProcessMode(params.getPostProcessMode());
        setPath(params.getPath());
        setWait(params.getWait());
    }
    public int getXThreads() {
        return xThreads;
    }
    private void setXThreads(int x_threads) {
        this.xThreads = MathUtils.clamp(x_threads, 1, initParams.getWidth());
    }
    public int getYThreads() {
        return yThreads;
    }
    private void setYThreads(int y_threads) {
        this.yThreads = MathUtils.clamp(y_threads, 1, initParams.getHeight());
    }
    public PixelContainer.PostProcessMode getPostProcessMode() {
        return postProcessMode;
    }
    public void setPostProcessMode(PixelContainer.PostProcessMode postProcessMode) {
        this.postProcessMode = postProcessMode;
    }
    public boolean useThreadedGenerator() {
        return (getXThreads() * getYThreads() > 1);
    }
    public void threadDataFromString(@NotNull String data) {
        @NotNull String[] parts = StringManipulator.split(data, ",");
        setXThreads(Integer.parseInt(parts[0]));
        setYThreads(Integer.parseInt(parts[1]));
    }
    public void setZoomConfig(@NotNull ZoomConfig config) {
        zoomConfig = new ZoomConfig(config);
    }
    @NotNull
    @Override
    public String toString() {
        return THREADS + xThreads + "," + yThreads + "\n" + POSTPROCESSING + postProcessMode + "\n" + ((zoomConfig != null) ? (zoomConfig + "\n") : "") + initParams + "\n" + runParams + "\n";
    }
}