package in.tamchow.fractal.config.fractalconfig.complex;
import java.io.Serializable;
/**
 * Encapsulates @code ComplexFractalInitParams and @code ComplexFractalRunParams
 */
public class ComplexFractalParams implements Serializable {
    public ComplexFractalRunParams runParams;
    public ComplexFractalInitParams initParams;
    public ComplexFractalParams() {
        runParams = new ComplexFractalRunParams(); initParams = new ComplexFractalInitParams();
    }
    public ComplexFractalParams(ComplexFractalInitParams initParams, ComplexFractalRunParams runParams) {
        this.initParams = new ComplexFractalInitParams(initParams);
        this.runParams = new ComplexFractalRunParams(runParams);
    }
    public ComplexFractalParams(ComplexFractalParams params) {
        this.initParams = new ComplexFractalInitParams(params.initParams);
        this.runParams = new ComplexFractalRunParams(params.runParams);
    }
}
