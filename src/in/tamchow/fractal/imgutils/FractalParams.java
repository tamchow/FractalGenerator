package in.tamchow.fractal.imgutils;

/**
 * Encapsulates @code FractalInitParams and @code FractalRunParams
 */
public class FractalParams {
    public FractalRunParams runParams;
    public FractalInitParams initParams;

    public FractalParams() {
        runParams = new FractalRunParams();
        initParams = new FractalInitParams();
    }

    public FractalParams(FractalInitParams initParams, FractalRunParams runParams) {
        this.initParams = new FractalInitParams(initParams);
        this.runParams = new FractalRunParams(runParams);
    }

    public FractalParams(FractalParams params) {
        this.initParams = new FractalInitParams(params.initParams);
        this.runParams = new FractalRunParams(params.runParams);
    }
}
