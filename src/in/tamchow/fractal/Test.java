package in.tamchow.fractal;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.BatchContainer;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalInitParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalRunParams;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.fractals.complexbrot.ComplexBrotFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.File;
/**
 * Test class, handles CMDLINE input.
 */
public class Test {
    public static void main(@NotNull String[] args) {
        @NotNull String func = "(z^3)+((d)*(z))+ e", variableCode = "z", poly = "1:z:3,+,d:z:1,+,e",
                poly2 = "1;tan;1:z:1;1", poly3 = "1:z:4,-,1", poly4 = "1:z:1;sin;1:z:2;1;-1", poly5 = "1:z:8,+,15:z:4,-,16", func2 = "z ^ 2 + f";
        @NotNull String[][] consts = {{"c", "-0.1,+0.651i"}, {"d", "-0.7198,+0.9111i"}, {"e", "-0.8,+0.156i"},
                {"f", "0.5,+0.25i"}, {"g", "1,+0.3i"}};
        int resx = 640, resy = 480, iter = 16, switch_rate = 0, num_points = 10000, max_hit_threshold = 10;
        @NotNull int[] iterations = {20};
        @NotNull ComplexFractalGenerator.Mode fracmode = ComplexFractalGenerator.Mode.NEWTON;
        double escrad = 1E10, tolerance = 1e-15, zoom = 10, zoompow = 0, baseprec = 60;
        @Nullable String linetrap = null;
        @NotNull Colorizer cfg = new Colorizer(Colors.MODE.NEWTON_CLASSIC, 4, 25000, 0, true, false);
        func = poly4;
        //cfg.setMultiplier_threshold(1E-6);
        //cfg.setExponentialSmoothing(false);
        //cfg.setPalette(new int[]{rgb(66, 30, 15), rgb(25, 7, 26), rgb(9, 1, 47), rgb(4, 4, 73), rgb(0, 7, 100), rgb(12, 44, 138), rgb(24, 82, 177), rgb(57, 125, 209), rgb(134, 181, 229), rgb(211, 236, 248), rgb(241, 233, 191), rgb(248, 201, 95), rgb(255, 170, 0), rgb(204, 128, 0), rgb(153, 87, 0), rgb(106, 52, 3)}, false);
        //cfg.createSmoothPalette(new int[]{rgb(0, 7, 100), rgb(32, 107, 203), rgb(237, 255, 255), rgb(255, 170, 0), rgb(0, 2, 0)}, new double[]{0.0, 0.16, 0.42, 0.6425, 0.8575});
        //cfg.setPalette(new int[]{Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.BLUE}, false);
        cfg.setPalette(new int[]{Colors.BASE_COLORS.GREEN, Colors.BASE_COLORS.BLUE, Colors.BASE_COLORS.RED, Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.MAGENTA, Colors.BASE_COLORS.CYAN, 0xff7fffd4, 0xffffa07a}, false);
        //cfg.createSmoothPalette(new int[]{Colors.BASE_COLORS.GREEN, Colors.BASE_COLORS.BLUE, Colors.BASE_COLORS.RED, Colors.BASE_COLORS.YELLOW, Colors.BASE_COLORS.MAGENTA, Colors.BASE_COLORS.CYAN, 0xff7fffd4, 0xffffa07a}, new double[]{0.12, 0.24, 0.36, 0.48, 0.6, 0.72, 0.84, 0.96});
        cfg.setColor_density(-1);//let there be the proper color_density!
        @Nullable Complex constant = null;//new Complex("1.0,+0.0i");
        @NotNull Complex trap = new Complex(1E-13);
        int x_t = 4, y_t = 2, xppp = 10, yppp = 10;
        double skew = 0 * Math.PI;
        boolean def = (args.length == 0);
        @Nullable BatchContainer<ComplexFractalParams> fccfg = new BatchContainer<>();
        if (!def) {
            try {
                fccfg = ConfigReader.getComplexFractalConfigFromFile(new File(args[0]));
            } catch (Exception e) {
                x_t = Integer.valueOf(args[0]);
                y_t = Integer.valueOf(args[1]);
                def = true;
            }
        }
        long inittime = System.currentTimeMillis();
        @Nullable ComplexFractalGenerator jgen;
        @Nullable ComplexFractalParams jgenParams = null;
        if (def) {
            jgenParams = new ComplexFractalParams(new ComplexFractalInitParams(resx, resy, zoom, zoompow, baseprec, fracmode, func, consts, variableCode, tolerance, cfg, switch_rate, trap), null, x_t, y_t);
            jgenParams.initParams.skew = skew;
            if (constant != null) {
                jgenParams.runParams = new ComplexFractalRunParams(iter, escrad, constant);
            } else {
                jgenParams.runParams = new ComplexFractalRunParams(iter, escrad);
            }
            jgen = new ComplexFractalGenerator(jgenParams, new DesktopProgressPublisher());
        } else {
            jgen = new ComplexFractalGenerator(fccfg.firstItem(), new DesktopProgressPublisher());
        }
        //jgen.zoom(new Complex(0.27969303810093984, -0.00838423653868096), 12);
        boolean anti = false, clamp = true;
        ComplexBrotFractalParams cbparams = new ComplexBrotFractalParams(resx, resy, x_t, y_t, switch_rate, xppp, yppp, max_hit_threshold, iterations, zoom, zoompow, baseprec, escrad, tolerance, skew, func, variableCode, consts, fracmode, anti, clamp);
        ComplexBrotFractalGenerator cbgen = new ComplexBrotFractalGenerator(cbparams, new DesktopProgressPublisher());
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        if (def) {
            /*if(cbparams.useThreadedGenerator()) {
                @NotNull ThreadedComplexBrotFractalGenerator cbthreaded = new ThreadedComplexBrotFractalGenerator(cbgen);
                cbthreaded.generate();
            }else{
                cbgen.generate();
            }*/
            if (jgenParams.useThreadedGenerator()) {
                @NotNull ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(jgen);
                threaded.generate();
            } else {
                jgen.generate();
            }
        } else {
            jgen.generate();
        }
        //System.out.println(cbgen.getDiscardedPointsCount()+" "+cbgen.getDiscardedPointsFraction());
        /*String ascii=jgen.createASCIIArt();
        //System.out.println(ascii);
        try {
            BufferedWriter writer=new BufferedWriter(new FileWriter("D:/output.txt",false));
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
            ImageIO.write(ImageConverter.toImage(jgen.getArgand().getPostProcessed(PixelContainer.PostProcessMode.NONE, jgen.getNormalized_escapes(), jgen.getColor().getByParts())), "png", pic);
            //ImageIO.write(ImageConverter.drawTextToImage(ascii,Font.MONOSPACED, Font.PLAIN, 0x000000, 0xffffff,10,0,0,jgen.getImageWidth(),jgen.getImageHeight()),"png",pic);
            //ImageIO.write(ImageConverter.toImage(cbgen.getPlane().getPostProcessed(PixelContainer.PostProcessMode.NONE, jgen.getNormalized_escapes(), jgen.getColor().getByParts())), "png", pic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endtime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endtime - gentime) + "ms");
        /*for(int i=0;i<jgen.getEscapedata().length;i++){
            for(int j=0;j<jgen.getEscapedata()[i].length;j++){
                System.out.print(jgen.getEscapedata()[i][j]+" <-> "+jgen.getNormalized_escapes()[i][j]+",");
            }System.out.println();
        }*/
    }
    static int rgb(int r, int g, int b) {
        return Colorizer.toRGB(r, g, b);
    }
}