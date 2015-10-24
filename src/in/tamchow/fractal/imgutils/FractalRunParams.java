package in.tamchow.fractal.imgutils;

/**
 * Parameters for configuring the generation of a fractal
 */
public class FractalRunParams {
    public int iterations, start_x, end_x, start_y, end_y;
    public double escape_radius, degree;
    public boolean fully_configured;

    public FractalRunParams(FractalRunParams runParams) {
        if (runParams.fully_configured) {
            initParams(runParams.start_x, runParams.end_x, runParams.start_y, runParams.end_y, runParams.iterations, runParams.escape_radius, runParams.degree);
            fully_configured = true;
        } else {
            initParams(runParams.iterations, runParams.escape_radius, runParams.degree);
            fully_configured = false;
        }
    }

    public FractalRunParams(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, double degree) {
        initParams(start_x, end_x, start_y, end_y, iterations, escape_radius, degree);
    }

    public FractalRunParams(int iterations, double escape_radius, double degree) {
        initParams(iterations, escape_radius, degree);
    }

    public FractalRunParams() {
        initParams(128, 2.0, 2.0);
    }

    public void initParams(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, double degree) {
        this.iterations = iterations;
        this.start_x = start_x;
        this.end_x = end_x;
        this.start_y = start_y;
        this.end_y = end_y;
        this.escape_radius = escape_radius;
        this.degree = degree;
        fully_configured = true;
    }

    public void initParams(int iterations, double escape_radius, double degree) {
        this.iterations = iterations;
        this.escape_radius = escape_radius;
        this.degree = degree;
        fully_configured = false;
    }

    public void paramsFromString(String[] params) {
        if (params.length == 7) {
            initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), Double.valueOf(params[5]), Double.valueOf(params[6]));
        } else if (params.length == 3) {
            initParams(Integer.valueOf(params[0]), Double.valueOf(params[1]), Double.valueOf(params[2]));
        }
    }
}
