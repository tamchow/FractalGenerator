package in.tamchow.fractal;
import in.tamchow.fractal.color.ColorData;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.InterpolationType;
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
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static in.tamchow.fractal.color.Colors.MODE.*;
import static in.tamchow.fractal.color.InterpolationType.LINEAR;
import static in.tamchow.fractal.color.InterpolationType.MONOTONE_CUBIC_SPLINE;
import static in.tamchow.fractal.fractals.complex.ComplexFractalGenerator.*;
/**
 * Test class, handles CMDLINE input.
 */
public class Test {
    public static void main(@NotNull String[] args) {
        @NotNull String func = "(z^3)+((d)*(z))+e", variableCode = "z", poly = "1:z:3,+,d:z:1,+,e",
                poly2 = "1;tan;1:z:1;1", poly3 = "1:z:2,+,e", poly4 = "1:z:1;sin;1:z:2;1;-1", poly5 = "1:z:8,+,15:z:4,-,16",
                func2 = "((z^2)/(1+(h*z)))+j", func3 = "z^2+e", magnet1 = "((z^2+c-1)/(2*z+c-2))^2",
                magnet2 = "((z^3+3*(c-1)*z+(c-1)*(c-2))/(3*(z^2)+ 3*(c-2)*z+(c-1)*(c-2)+1))^2",
                experimental1 = "e^(-z)", experimental2 = "e^z", experimental3 = "exp(z)", experimental4 = "z^(-z)",
                experimental5 = "z^z", experimental6 = "log(z)", experimental7 = "exp(z^3)", experimental8 = "exp(z^z)",
                experimental9 = "exp(z^(-z))", experimental10 = "(-z)^(exp(z))";
        @NotNull String[][] consts = {{"c", "-0.1,+0.651i"}, {"d", "-0.7198,+0.9111i"}, {"e", "-0.8,+0.156i"},
                {"f", "0.5,+0.25i"}, {"g", "1,+0.3i"}, {"h", "-0.2013,+0.5638i"}, {"j", "1.4686,+1.265i"}};
        int width = 640, height = 480, maxIterations = 32, switchRate = 0, numPoints = 10_000, maxHitThreshold = 10;
        @NotNull int[] iterations = {20};
        @Nullable double[] percentiles = null;
        @NotNull Mode fractalMode = Mode.JULIA;
        double escapeRadius = 1e10, tolerance = 1e-10, zoom = 0.25, basePrecision = -1;
        @Nullable String linetrap = null;
        @NotNull ColorData colorData = new ColorData(TRIANGLE_AREA_INEQUALITY, -1, 16_700_000, 0, true,
                false, MONOTONE_CUBIC_SPLINE, false, false,
                false, -1);
        func = experimental4;
        //colorData.setModifierEnabled(true);
        //colorData.setMultiplier_threshold(1E-6);
        //colorData.setExponentialSmoothing(false);
        //colorData.setLogIndex(false);
        //colorData.setPalette(new int[]{rgb(66, 30, 15), rgb(25, 7, 26), rgb(9, 1, 47), rgb(4, 4, 73), rgb(0, 7, 100), rgb(12, 44, 138), rgb(24, 82, 177), rgb(57, 125, 209), rgb(134, 181, 229), rgb(211, 236, 248), rgb(241, 233, 191), rgb(248, 201, 95), rgb(255, 170, 0), rgb(204, 128, 0), rgb(153, 87, 0), rgb(106, 52, 3)}, false);
        //colorData.createSmoothPalette(new int[]{rgb(0, 7, 100), rgb(32, 107, 203), rgb(237, 255, 255), rgb(255, 170, 0), rgb(0, 2, 0), rgb(0, 7, 100)}, new double[]{0.0, 0.16, 0.42, 0.6425, 0.8575, 1.0});
        //colorData.setPalette(new int[]{Colors.BaseColors.YELLOW, Colors.BaseColors.BLUE, Colors.BaseColors.RED, Colors.BaseColors.GREEN}, false);
        //colorData.setPalette(new int[]{Colors.BaseColors.GREEN, Colors.BaseColors.BLUE, Colors.BaseColors.RED, Colors.BaseColors.YELLOW}, false);
        colorData.createSmoothPalette(new int[]{Colors.BaseColors.RED, Colors.BaseColors.BLUE, Colors.BaseColors.GREEN, Colors.BaseColors.YELLOW, Colors.BaseColors.MAGENTA, Colors.BaseColors.CYAN, 0xff7fffd4, 0xffffa07a}, new double[]{0.12, 0.24, 0.36, 0.48, 0.6, 0.72, 0.84, 0.96});
        //colorData.createSmoothPalette(new int[]{Colors.BaseColors.WHITE, Colors.BaseColors.BLACK, Colors.BaseColors.RED, Colors.BaseColors.YELLOW, Colors.BaseColors.GREEN, Colors.BaseColors.CYAN, Colors.BaseColors.BLUE, Colors.BaseColors.MAGENTA, Colors.BaseColors.WHITE}, new double[]{0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0});
        /*BufferedImage img = ImageConverter.toImage(ColorDebugger.createDebugImage(colorData.getPalette()));
        try {
            ImageIO.write(img, "png", new File(new File(".").getAbsoluteFile().getAbsolutePath() + "/ColorDebug.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1);*/
        @Nullable Complex constant = null;//new Complex("1.0,+0.0i");
        @NotNull Complex trap = new Complex(1);
        int x_t = 2, y_t = 4, xppp = 10, yppp = 10;
        double skew = 0 * Math.PI;
        boolean defined = (args.length == 0);
        @Nullable ComplexFractalParams jgenParams = null;
        if (!defined) {
            try {
                jgenParams = ConfigReader.getComplexParamFromFile(new File(args[0]));
            } catch (Exception e) {
                x_t = Integer.parseInt(args[0]);
                y_t = Integer.parseInt(args[1]);
                defined = true;
            }
        }
        long initTime = System.currentTimeMillis();
        @Nullable ComplexFractalGenerator jgen;
        if (defined) {
            jgenParams = new ComplexFractalParams(new ComplexFractalInitParams(width, height, zoom, basePrecision, fractalMode, func, consts, variableCode, tolerance, colorData, switchRate, trap), null, x_t, y_t);
            jgenParams.initParams.skew = skew;
            jgenParams.setPostProcessMode(PixelContainer.PostProcessMode.NONE);
            if (constant != null) {
                jgenParams.runParams = new ComplexFractalRunParams(maxIterations, escapeRadius, constant);
            } else {
                jgenParams.runParams = new ComplexFractalRunParams(maxIterations, escapeRadius);
            }
        }
        jgen = new ComplexFractalGenerator(jgenParams, new DesktopProgressPublisher());
        //jgen.zoom(new Matrix(new double[][]{{-2.0, -1.25}, {1.5, 1.25}}));
        //jgen.zoom(new Matrix(new double[][]{{-0.74877, 0.065053}, {-0.74872, 0.065103}}));
        //jgen.zoom(880, 840, 10);
        //jgen.zoom(780, 420, 10);
        //jgen.zoom(680, 560, 10);
        //jgen.zoom(1100, 790, 10);
        //jgen.zoom(1375, 695, 10);
        //jgen.zoom(new Complex(0.27969303810093984, -0.00838423653868096), 1E12);
        boolean anti = false, clamp = true;
        //ComplexBrotFractalParams cbparams = new ComplexBrotFractalParams(width, height, x_t, y_t, switchRate, xppp, yppp, maxHitThreshold, iterations, zoom, zoompow, basePrecision, escapeRadius, tolerance, skew, func, variableCode, constants, fractalMode, anti, clamp, percentiles);
        //ComplexBrotFractalGenerator cbgen = new ComplexBrotFractalGenerator(cbparams, new DesktopProgressPublisher());
        long startTime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (startTime - initTime) + "ms");
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
        long generationTime = System.currentTimeMillis();
        //System.out.println(jgen.getRoots());
        //System.out.println(jgen.getColor().averageTint());
        System.out.println(jgen.averageIterations());
        PixelContainer image = jgen.getArgand().getPostProcessed(
                jgenParams != null ? jgenParams.getPostProcessMode() : PixelContainer.PostProcessMode.NONE,
                jgen.getNormalizedEscapes(), jgen.getColor().getByParts(),
                jgen.getColor().getInterpolationType(), jgen.getColor().isGammaCorrection());
//        String csv = simpleArrayToString(image.toHeightMap().generateHeightField());
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(".").getAbsoluteFile().getAbsolutePath() + "/output.csv", false));
//            writer.write(csv);
//            writer.flush();
//            writer.close();
//        } catch (IOException ignored) {
//            ignored.printStackTrace();
//        }
        System.out.println("Generating fractal took:" + ((double) (generationTime - startTime) / 60000) + "mins");
        @NotNull File pic = new File(new File(".").getAbsoluteFile().getAbsolutePath() + "/Fractal.png");
        try {
            ImageIO.write(ImageConverter.toImage(image), "png", pic);
            //ImageIO.write(ImageConverter.drawTextToImage(ascii, Font.MONOSPACED, Font.PLAIN, 0xff000000, 0xffffffff, 10, 0, 0, jgen.getImageWidth(), jgen.getImageHeight()), "png", pic);
            //ImageIO.write(ImageConverter.toImage(cbgen.getPlane().getPostProcessed(PixelContainer.PostProcessMode.NONE, jgen.getNormalized_escapes(), jgen.getColor().getByParts())), "png", pic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endTime - generationTime) + "ms");
    }
    static int rgb(int r, int g, int b) {
        return ColorData.toRGB(r, g, b);
    }
    private static String simpleArrayToString(Object[] array) {
        StringBuilder stringBuilder = new StringBuilder(array.length * array[0].toString().length());
        for (Object item : array) {
            stringBuilder.append(item).append("\n");
        }
        return stringBuilder.toString();
    }
}