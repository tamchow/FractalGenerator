package in.tamchow.fractal;

import in.tamchow.fractal.complex.Complex;
import in.tamchow.fractal.complex.FunctionEvaluator;
import in.tamchow.fractal.imgutils.*;

import java.util.ArrayList;

/**
 * The actual Julia Set plotter program.
 * Not really sure about the zoom function, let's see if it works.
 * TODO:
 * Zoom works,but it is damn slow. Got no idea as to how to make it faster. Maybe prerender zooms on a separate thread?
 * NOTE:
 * Extended to do Mandelbrot set too.
 */
public class FractalGenerator {
    public static final int MODE_MANDELBROT = 0, MODE_JULIA = 1;
    ArrayList<Complex> boundary_points;
    int zoom;
    int zoom_factor;
    int base_precision;
    int scale;
    int color_mode;
    int num_colors;
    double boundary_condition;
    long maxiter;
    int mode;
    int color_density;
    ImageData argand;
    String function;
    String[][] consts;
    Complex[][]argand_map;
    int center_x, center_y;
    int[] random_palette, gradient_palette;

    public long getMaxiter() {
        return maxiter;
    }

    public void setMaxiter(long maxiter) {
        this.maxiter = maxiter;
    }

    public FractalGenerator(FractalParams params){
        initFractal(params.initParams.width,params.initParams.height,params.initParams.zoom,params.initParams.zoom_factor,params.initParams.base_precision,params.initParams.color_mode,params.initParams.num_colors,params.initParams.color_density,params.initParams.fractal_mode,params.initParams.boundary_condition,params.initParams.function,params.initParams.consts);
    }
    private void initFractal(int width, int height, int zoom, int zoom_factor, int base_precision, int colorizer, int num_colors, int color_density, int mode, double boundary_condition, String function, String[][] consts) {
        setZoom(zoom);
        setZoom_factor(zoom_factor);
        setFunction(function);
        setBase_precision(base_precision);
        setConsts(consts);
        setScale((int) (base_precision * Math.pow(zoom, zoom_factor)));
        boundary_points = new ArrayList<>();
        argand = new ImageData(width, height);
        setColor_mode(colorizer);
        setColor_density(color_density);
        setNum_colors(num_colors);
        random_palette = new int[num_colors];
        random_palette[0] = 0x000000;
        gradient_palette = new int[num_colors];
        gradient_palette[0] = 0x000000;
        for (int pidx = 0x1; pidx < num_colors; pidx++) {
            random_palette[pidx] = (((int) (Math.random() * 255)) << 16 | ((int) (Math.random() * 255)) << 8 | ((int) (Math.random() * 255)));
            if (colorizer == ColorMode.COLOR_GRADIENT_DIVERGENT_MODE_1) {
                gradient_palette[pidx] = gradient_palette[pidx - 1] + (0xfffff / (color_density << num_colors));
            } else if (colorizer == ColorMode.COLOR_GRADIENT_DIVERGENT_MODE_2) {
                gradient_palette[pidx] = gradient_palette[pidx - 1] + (0xfffff >> num_colors);
            }
        }
        setBoundary_condition(boundary_condition);
        setMode(mode);
        if (mode == MODE_MANDELBROT) {
            consts[0][0] = "c";
            consts[0][1] = "0,+0i";
        }
        setCenter_x(argand.getWidth() / 2);
        setCenter_y(argand.getHeight() / 2);
        setMaxiter(argand.getHeight() * argand.getWidth());
        argand_map=new Complex[argand.getHeight()][argand.getWidth()];
        poupulateMap();
    }
    public FractalGenerator(int width, int height, int zoom, int zoom_factor, int base_precision, int colorizer, int num_colors, int color_density, int mode, double boundary_condition, String function, String[][] consts) {
        initFractal(width,height,zoom,zoom_factor,base_precision,colorizer,num_colors,color_density,mode,boundary_condition,function,consts);
    }


    private void poupulateMap() {
        for (int i=0;i<argand.getHeight();i++){
            for (int j=0;j<argand.getWidth();j++){
                argand_map[i][j]=fromCooordinates(j,i);
            }
        }
    }

    public int getColor_density() {
        return color_density;
    }

    public void setColor_density(int color_density) {
        this.color_density = color_density;
    }

    public int getNum_colors() {
        return num_colors;
    }

    public void setNum_colors(int num_colors) {
        this.num_colors = num_colors;
    }

    public double getBoundary_condition() {
        return boundary_condition;
    }

    public void setBoundary_condition(double boundary_condition) {
        this.boundary_condition = boundary_condition;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int[] start_end_coordinates(int nx, int ix, int iy, int ny) {
        int start_x, end_x, start_y, end_y;
        int x_dist = argand.getWidth() / nx, y_dist = argand.getHeight() / ny;
        if (ix == (nx - 1)) {
            start_x = (nx - 1) * x_dist;
            end_x = argand.getWidth();
        } else {
            start_x = ix * x_dist;
            end_x = (ix + 1) * x_dist;
        }
        if (iy == (ny - 1)) {
            start_y = (ny - 1) * y_dist;
            end_y = argand.getHeight();
        } else {
            start_y = iy * y_dist;
            end_y = (iy + 1) * y_dist;
        }
        return new int[]{start_x, end_x, start_y, end_y};
    }

    private int interpolate(int color1, int color2, double bias) {
        return (int) (color1 * bias + color2 * (1 - bias));
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int getZoom_factor() {
        return zoom_factor;
    }

    public void setZoom_factor(int zoom_factor) {
        this.zoom_factor = zoom_factor;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public int getBase_precision() {
        return base_precision;
    }

    public void setBase_precision(int base_precision) {
        this.base_precision = base_precision;
    }

    public ImageData getArgand() {
        return argand;
    }

    public void setArgand(ImageData argand) {
        this.argand = new ImageData(argand);
    }

    public String[][] getConsts() {
        return consts;
    }

    public void setConsts(String[][] consts) {
        this.consts = new String[consts.length][consts[0].length];
        for (int i = 0; i < consts.length; i++) {
            System.arraycopy(consts[i], 0, this.consts[i], 0, consts[i].length);
        }
    }

    public int getCenter_x() {
        return center_x;
    }

    public void setCenter_x(int center_x) {
        this.center_x = center_x;
    }

    public int getCenter_y() {
        return center_y;
    }

    public void setCenter_y(int center_y) {
        this.center_y = center_y;
    }

    public int getColor_mode() {
        return color_mode;
    }

    public void setColor_mode(int color_mode) {
        this.color_mode = color_mode;
    }

    public void generate(FractalParams params){
        if (params.runParams.fully_configured){
            generate(params.runParams.start_x,params.runParams.end_x,params.runParams.start_y,params.runParams.end_y,params.runParams.iterations,params.runParams.escape_radius,params.runParams.degree);
        }else{
            generate(params.runParams.iterations,params.runParams.escape_radius,params.runParams.degree);
        }
    }
    public void generate(int iterations, double escape_radius, double degree) {
        generate(0, argand.getWidth(), 0, argand.getHeight(), iterations, escape_radius, degree);
    }

    public void generate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, double degree) {
        setMaxiter(argand.getHeight() * argand.getHeight() * iterations);
        if (mode == MODE_MANDELBROT) {
            mandelbrotGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius, degree);
        } else if (mode == MODE_JULIA) {
            juliaGenerate(start_x, end_x, start_y, end_y, iterations, escape_radius, degree);
        }
    }

    public void mandelbrotGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, double degree) {
        boundary_points.clear();
        FunctionEvaluator fe = new FunctionEvaluator("0,+0i", consts);
        long ctr = 0;
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = new Complex("0,+0i");
                consts[0][1] = argand_map[i][j].toString();
                if (argand_map[i][j].modulus() == boundary_condition) {
                    boundary_points.add(argand_map[i][j]);
                }
                fe.setZ_value(z.toString());
                fe.setConstdec(consts);
                int c = 0x1;
                while (c <= iterations && z.modulus() < escape_radius) {
                    Complex ztmp = fe.evaluate(function);
                    if (ztmp.equals(z)) {
                        c = iterations + 1;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    System.out.println(ctr+" iterations of "+maxiter);
                    c++;
                    if(ctr>maxiter){
                        break;
                    }
                    ctr++;
                }
                argand.setPixel(i, j, getColor(c, z, degree, escape_radius));
            }
        }
    }

    public void juliaGenerate(int start_x, int end_x, int start_y, int end_y, int iterations, double escape_radius, double degree) {
        boundary_points.clear();
        FunctionEvaluator fe = new FunctionEvaluator("0,+0i", consts);
        long ctr = 0;
        for (int i = start_y; i < end_y; i++) {
            for (int j = start_x; j < end_x; j++) {
                Complex z = argand_map[i][j];
                if (z.modulus() == boundary_condition) {
                    boundary_points.add(z);
                }
                int c = 0x1;
                fe.setZ_value(z.toString());
                while (c <= iterations && z.modulus() < escape_radius) {
                    Complex ztmp = fe.evaluate(function);
                    if (ztmp.equals(z)) {
                        c = iterations + 1;
                        break;
                    }
                    z = new Complex(ztmp);
                    fe.setZ_value(z.toString());
                    System.out.println(ctr+" iterations of "+maxiter);
                    c++;
                    if(ctr>maxiter){
                        break;
                    }
                    ctr++;
                }
                argand.setPixel(i, j, getColor(c, z, degree, escape_radius));
            }
        }
    }

    public int getColor(int val, Complex z, double degree, double escape_radius) {
        int color = 0x0, color1 = 0x0, color2 = 0x0;
        double renormalized = ((val + 1) - (Math.log(Math.log(z.modulus() / Math.log(escape_radius)) / Math.log(degree))));
        switch (color_mode) {
            case ColorMode.COLOR_DIVIDE:
                color1 = (int) (0xffffff / renormalized);
                color2 = (int) (0xffffff / (renormalized + 1));
                color = interpolate(color1, color2, renormalized - ((int) renormalized));
                break;
            case ColorMode.COLOR_MULTIPLY:
                color1 = (int) renormalized << 16 | (int) renormalized << 8 | (int) renormalized;
                color2 = (int) (renormalized + 1) << 16 | (int) (renormalized + 1) << 8 | (int) (renormalized + 1);
                color = interpolate(color1, color2, renormalized - ((int) renormalized));
                break;
            case ColorMode.COLOR_GRAYSCALE:
                color = val << 16 | val << 8 | val;
                break;
            /*case ColorMode.COLOR_MULTIPLY_MODE_2:
                color1=((int)renormalized<<16)<<16|((int)renormalized<<8)<<8|(int)renormalized;
                color2=((int)(renormalized+1)<<16)<<16|((int)(renormalized+1)<<8)<<8|(int)(renormalized+1);
                color=interpolate(color1,color2,renormalized-((int)renormalized));
                break;
            case ColorMode.COLOR_MULTIPLY_MODE_3:
                color1=((int)renormalized)<<16|((int)renormalized<<8)<<8|((int)renormalized<<16);
                color2=((int)(renormalized+1))<<16|((int)(renormalized+1)<<8)<<8|((int)(renormalized+1)<<16);
                color=interpolate(color1,color2,renormalized-((int)renormalized));
                break;
                */
            case ColorMode.COLOR_GRADIENT_DIVERGENT_MODE_1:
            case ColorMode.COLOR_GRADIENT_DIVERGENT_MODE_2:
                color1 = gradient_palette[(int) ((renormalized * color_density) % num_colors)];
                color2 = color1 + 1;
                color = interpolate(color1, color2, renormalized - (int) (renormalized));
                break;
            case ColorMode.COLOR_RANDOM_DIVERGENT:
                renormalized = ((val + 1) - (Math.log(Math.log(z.modulus() / Math.log(escape_radius)) / Math.log(degree))));
                color1 = random_palette[(int) ((renormalized * color_density) % num_colors)];
                color2 = color1 + 1;
                color = interpolate(color1, color2, renormalized - (int) (renormalized));
                break;
            default:
                throw new IllegalArgumentException("invalid argument");
        }
        return color;
    }

    public Complex fromCooordinates(int x, int y) {
        return new Complex(((((double) x) - center_x) / scale), ((center_y - ((double) y)) / scale));
    }

    public int[] toCooordinates(Complex point) {
        int x=(int) ((point.real() * scale) + center_x),y=(int) (center_y - (point.imaginary() * scale));
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > argand.getWidth()) {
            x = argand.getWidth();
        }
        if (y > argand.getHeight()) {
            y = argand.getHeight();
        }
        return new int[]{x,y};
    }

    public void zoom(int cx, int cy, int level) {
        if (cx < 0) {
            cx = 0;
        }
        if (cy < 0) {
            cy = 0;
        }
        if (cx > argand.getWidth()) {
            cx = argand.getWidth();
        }
        if (cy > argand.getHeight()) {
            cy = argand.getHeight();
        }
        setCenter_y(cy);
        setCenter_x(cx);
        setZoom_factor(level);
        int precision = (argand.getHeight() >= argand.getWidth()) ? argand.getWidth() / 2 : argand.getHeight() / 2;
        setBase_precision(precision);
        setScale((int) (base_precision * Math.pow(zoom, zoom_factor)));
        boundary_points.clear();
        poupulateMap();
    }
}