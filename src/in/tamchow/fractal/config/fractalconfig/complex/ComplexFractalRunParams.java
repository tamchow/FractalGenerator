package in.tamchow.fractal.config.fractalconfig.complex;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;

import static in.tamchow.fractal.config.Strings.BLOCKS.ENDRUN;
import static in.tamchow.fractal.config.Strings.BLOCKS.RUN;
/**
 * Parameters for configuring the generation of a fractal
 */
public class ComplexFractalRunParams implements Serializable, DataFromString {
    public int startX, endX, startY, endY;
    public int iterations;
    public double escapeRadius;
    @Nullable
    public Complex constant;
    public boolean fullyConfigured;
    public ComplexFractalRunParams(@NotNull ComplexFractalRunParams runParams) {
        if (runParams.fullyConfigured) {
            initParams(runParams.startX, runParams.endX, runParams.startY, runParams.endY, runParams.iterations, runParams.escapeRadius, runParams.constant);
            fullyConfigured = true;
        } else {
            initParams(runParams.iterations, runParams.escapeRadius, runParams.constant);
            fullyConfigured = false;
        }
    }
    public ComplexFractalRunParams(int startX, int endX, int startY, int endY, int iterations, double escapeRadius) {
        initParams(startX, endX, startY, endY, iterations, escapeRadius);
    }
    public ComplexFractalRunParams(int startX, int endX, int startY, int endY, int iterations, double escapeRadius, @NotNull Complex constant) {
        initParams(startX, endX, startY, endY, iterations, escapeRadius, constant);
    }
    public ComplexFractalRunParams(int iterations, double escapeRadius) {
        initParams(iterations, escapeRadius);
    }
    public ComplexFractalRunParams(int iterations, double escapeRadius, Complex constant) {
        initParams(iterations, escapeRadius, constant);
    }
    public ComplexFractalRunParams() {
        initParams(128, 2.0);
    }
    public void initParams(int startX, int endX, int startY, int endY, int iterations, double escapeRadius, @Nullable Complex constant) {
        setIterations(iterations);
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.escapeRadius = escapeRadius;
        this.constant = constant;
        fullyConfigured = true;
    }
    public void initParams(int iterations, double escapeRadius, @Nullable Complex constant) {
        setIterations(iterations);
        this.escapeRadius = escapeRadius;
        this.constant = constant;
        fullyConfigured = false;
    }
    public void initParams(int startX, int endX, int startY, int endY, int iterations, double escapeRadius) {
        initParams(startX, endX, startY, endY, iterations, escapeRadius, null);
    }
    public void initParams(int iterations, double escapeRadius) {
        setIterations(iterations);
        this.escapeRadius = escapeRadius;
        constant = null;
        fullyConfigured = false;
    }
    public void setIterations(int iterations) {
        this.iterations = MathUtils.clamp(iterations, 0, Integer.MAX_VALUE - 2);
    }
    @Override
    public String toString() {
        if (fullyConfigured) {
            return String.format(RUN + "%n%d%n%d%n%d%n%d%n%d%n%f%n%s%n" + ENDRUN, startX, endX, startY, endY, iterations, escapeRadius, (constant == null) ? "" : constant);
        }
        return String.format(RUN + "%n%d%n%f%n%s%n" + ENDRUN, iterations, escapeRadius, (constant == null) ? "" : constant);
    }
    /**
     * @param params: Pass in -1 for escapeRadius in case of Newton Fractal Mode
     */
    public void fromString(@NotNull String[] params) {
        switch (params.length) {
            case 6:
                initParams(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Double.parseDouble(params[5]));
                break;
            case 7:
                initParams(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Double.parseDouble(params[5]), new Complex(params[6]));
                break;
            case 2:
                initParams(Integer.parseInt(params[0]), Double.parseDouble(params[1]));
                break;
            case 3:
                initParams(Integer.parseInt(params[0]), Double.parseDouble(params[1]), new Complex(params[2]));
                break;
        }
    }
}