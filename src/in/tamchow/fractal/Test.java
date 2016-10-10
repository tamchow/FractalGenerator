package in.tamchow.fractal;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalInitParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalRunParams;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.math.matrix.Matrix;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.File;
/**
 * Test class, handles CMDLINE input.
 */
public class Test {
    public static void main(@NotNull String[] args) {
        @NotNull String func = "(z^3)+((d)*(z))+e", variableCode = "z", poly = "1:z:3,+,d:z:1,+,e",
                poly2 = "1;tan;1:z:1;1", poly3 = "1:z:4,-,1", poly4 = "1:z:1;sin;1:z:2;1;-1", poly5 = "1:z:8,+,15:z:4,-,16",
                func2 = "z^2+e", magnet1 = "((z^2+c-1)/(2*z+c-2))^2",
                magnet2 = "((z^3+3*(c-1)*z+(c-1)*(c-2))/(3*(z^2)+3*(c-2)*z+(c-1)*(c-2)+1))^2";
        @NotNull String[][] consts = {{"c", "-0.1,+0.651i"}, {"d", "-0.7198,+0.9111i"}, {"e", "-0.8,+0.156i"},
                {"f", "0.5,+0.25i"}, {"g", "1,+0.3i"}};
        int resx = 1001, resy = 1001, iter = 16, switch_rate = 0, num_points = 10000, max_hit_threshold = 10;
        @NotNull int[] iterations = {20};
        @Nullable double[] percentiles = null;
        @NotNull ComplexFractalGenerator.Mode fracmode = ComplexFractalGenerator.Mode.MANDELBROT;
        double escrad = 2, tolerance = 1e-15, zoom = 1, baseprec = -1;
        @Nullable String linetrap = null;
        @NotNull Colorizer cfg = new Colorizer(Colors.MODE.STRIPE_AVERAGE, 4, 25000000, 0, true, false, false, false, false, false, -1);
        func = func2;
        //cfg.setModifierEnabled(true);
        //cfg.setMultiplier_threshold(1E-6);
        //cfg.setExponentialSmoothing(false);
        //cfg.setPalette(new int[]{rgb(66, 30, 15), rgb(25, 7, 26), rgb(9, 1, 47), rgb(4, 4, 73), rgb(0, 7, 100), rgb(12, 44, 138), rgb(24, 82, 177), rgb(57, 125, 209), rgb(134, 181, 229), rgb(211, 236, 248), rgb(241, 233, 191), rgb(248, 201, 95), rgb(255, 170, 0), rgb(204, 128, 0), rgb(153, 87, 0), rgb(106, 52, 3)}, false);
        cfg.createSmoothPalette(new int[]{rgb(0, 7, 100), rgb(32, 107, 203), rgb(237, 255, 255), rgb(255, 170, 0), rgb(0, 2, 0)}, new double[]{0.0, 0.16, 0.42, 0.6425, 0.8575});
        //cfg.setPalette(new int[]{Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.BLUE}, false);
        //cfg.setPalette(new int[]{Colors.BASE_COLORS.GREEN, Colors.BASE_COLORS.BLUE, Colors.BASE_COLORS.RED, Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.MAGENTA, Colors.BASE_COLORS.CYAN, 0xff7fffd4, 0xffffa07a}, false);
        //cfg.createSmoothPalette(new int[]{Colors.BASE_COLORS.GREEN, Colors.BASE_COLORS.BLUE, Colors.BASE_COLORS.RED, Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.MAGENTA, Colors.BASE_COLORS.CYAN, 0xff7fffd4, 0xffffa07a}, new double[]{0.12, 0.24, 0.36, 0.48, 0.6, 0.72, 0.84, 0.96});
        //cfg.createSmoothPalette(new int[]{Colors.BASE_COLORS.RED, Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.GREEN, Colors.BASE_COLORS.BLUE}, new double[]{0.2, 0.4, 0.6, 0.8});
        /*BufferedImage img=ImageConverter.toImage(ColorDebugger.createDebugImage(cfg.getPalette()));
        try{
            ImageIO.write(img,"png",new File(new File(".").getAbsoluteFile().getAbsolutePath()+"/ColorDebug.png"));
        }catch(IOException e){
            e.printStackTrace();
        }
        System.exit(1);*/
        @Nullable Complex constant = null;//new Complex("1.0,+0.0i");
        @NotNull Complex trap = new Complex(1);
        int x_t = 4, y_t = 2, xppp = 10, yppp = 10;
        double skew = 0 * Math.PI;
        boolean def = (args.length == 0);
        @Nullable ComplexFractalParams jgenParams = null;
        if (!def) {
            try {
                jgenParams = ConfigReader.getComplexParamFromFile(new File(args[0]));
            } catch (Exception e) {
                x_t = Integer.valueOf(args[0]);
                y_t = Integer.valueOf(args[1]);
                def = true;
            }
        }
        long inittime = System.currentTimeMillis();
        @Nullable ComplexFractalGenerator jgen;
        if (def) {
            jgenParams = new ComplexFractalParams(new ComplexFractalInitParams(resx, resy, zoom, baseprec, fracmode, func, consts, variableCode, tolerance, cfg, switch_rate, trap), null, x_t, y_t);
            jgenParams.initParams.skew = skew;
            if (constant != null) {
                jgenParams.runParams = new ComplexFractalRunParams(iter, escrad, constant);
            } else {
                jgenParams.runParams = new ComplexFractalRunParams(iter, escrad);
            }
        }
        jgen = new ComplexFractalGenerator(jgenParams, new DesktopProgressPublisher());
        jgen.zoom(new Matrix(new double[][]{{-2.0, -1.25}, {1.5, 1.25}}));
        //jgen.zoom(new Matrix(new double[][]{{-0.74877, 0.065053}, {-0.74872, 0.065103}}));
        //jgen.zoom(1255, 540, 10);
        //jgen.zoom(1230, 290, 10);
        //jgen.zoom(1650, 450, 10);
        //jgen.zoom(1100, 790, 10);
        //jgen.zoom(1375, 695, 10);
        //jgen.zoom(new Complex(0.27969303810093984, -0.00838423653868096), 1E12);
        boolean anti = false, clamp = true;
        //ComplexBrotFractalParams cbparams = new ComplexBrotFractalParams(resx, resy, x_t, y_t, switch_rate, xppp, yppp, max_hit_threshold, iterations, zoom, zoompow, baseprec, escrad, tolerance, skew, func, variableCode, consts, fracmode, anti, clamp, percentiles);
        //ComplexBrotFractalGenerator cbgen = new ComplexBrotFractalGenerator(cbparams, new DesktopProgressPublisher());
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        /*@NotNull ThreadedComplexBrotFractalGenerator cbthreaded = new ThreadedComplexBrotFractalGenerator(cbgen);
        cbthreaded.generate();*/
        @NotNull ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(jgen);
        threaded.generate();
        //System.out.println(cbgen.getDiscardedPointsCount()+" "+cbgen.getDiscardedPointsFraction());
        /*String ascii=jgen.createASCIIArt();
        //System.out.println(ascii);
        try {
            BufferedWriter writer=new BufferedWriter(new FileWriter(new File(".").getAbsoluteFile().getAbsolutePath() + "output.txt",false));
            writer.write(ascii);
            writer.flush();
            writer.close();
        }catch (IOException ignored){
            ignored.printStackTrace();
        }*/
        long gentime = System.currentTimeMillis();
        //System.out.println(jgen.getRoots());
        //System.out.println(jgen.getColor().averageTint());
        System.out.println(jgen.averageIterations());
        System.out.println("Generating fractal took:" + ((double) (gentime - starttime) / 60000) + "mins");
        @NotNull File pic = new File(new File(".").getAbsoluteFile().getAbsolutePath() + "/Fractal.png");
        try {
            ImageIO.write(ImageConverter.toImage(jgen.getArgand().getPostProcessed(
                    jgenParams != null ? jgenParams.getPostProcessMode() : PixelContainer.PostProcessMode.MEDIAN,
                    jgen.getNormalized_escapes(), jgen.getColor().getByParts(),
                    jgen.getColor().isGammaCorrection())), "png", pic);
            //ImageIO.write(ImageConverter.drawTextToImage(ascii, Font.MONOSPACED, Font.PLAIN, 0xff000000, 0xffffffff, 10, 0, 0, jgen.getImageWidth(), jgen.getImageHeight()), "png", pic);
            //ImageIO.write(ImageConverter.toImage(cbgen.getPlane().getPostProcessed(PixelContainer.PostProcessMode.NONE, jgen.getNormalized_escapes(), jgen.getColor().getByParts())), "png", pic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endtime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endtime - gentime) + "ms");
    }
    static int rgb(int r, int g, int b) {
        return Colorizer.toRGB(r, g, b);
    }
}