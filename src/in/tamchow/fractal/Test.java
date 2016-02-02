package in.tamchow.fractal;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalInitParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalRunParams;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.File;
/**
 * Test class, handles CMDLINE input.
 */
public class Test {
    public static void main(String[] args) {
        String func = "( z ^ 3 ) + ( ( d ) * ( z ) ) + e", variableCode = "z", poly = "{1:z:3};+;{d:z:1};+;{e:z:0}", poly2 = "{f:z:0};sin;{1:z:1}", func2 = "z ^ 3 + e";
        String[][] consts = {{"c", "-0.1,+0.651i"}, {"d", "-0.7198,+0.9111i"}, {"e", "-0.8,+0.156i"}, {"f", "0.5,+0.25i"}, {"g", "1,+0.3i"}};
        int resx = 1920, resy = 1080, iter = 32, switch_rate = 0;
        ComplexFractalGenerator.Mode fracmode = ComplexFractalGenerator.Mode.RUDY;
        double escrad = 2, tolerance = 1e-15, zoom = 10, zoompow = 0, baseprec = 200; String linetrap = null;
        ColorConfig cfg = new ColorConfig(Colors.CALCULATIONS.STRIPE_AVERAGE_SPLINE, 4, 2500, true, true, true);
        //cfg.setExponentialSmoothing(false);
        //cfg.setPalette(new int[]{rgb(66, 30, 15), rgb(25, 7, 26), rgb(9, 1, 47), rgb(4, 4, 73), rgb(0, 7, 100), rgb(12, 44, 138), rgb(24, 82, 177), rgb(57, 125, 209), rgb(134, 181, 229), rgb(211, 236, 248), rgb(241, 233, 191), rgb(248, 201, 95), rgb(255, 170, 0), rgb(204, 128, 0), rgb(153, 87, 0), rgb(106, 52, 3)}, false);
        cfg.createSmoothPalette(new int[]{rgb(0, 7, 100), rgb(32, 107, 203), rgb(237, 255, 255), rgb(255, 170, 0), rgb(0, 2, 0)}, new double[]{0.0, 0.16, 0.42, 0.6425, 0.8575});
        //cfg.setPalette(new int[]{0xff0000, 0x00ff00, 0x0000ff, 0xfff000}, false);
        //cfg.createSmoothPalette(new int[]{0xffff0000, 0xff00ff00, 0xff0000ff, 0xfffff000}, new double[]{0.2, 0.4, 0.6, 0.8});
        //cfg.setColor_density(-1);
        Complex constant = null;//new Complex("1.0,+0.0i");
        Complex trap = new Complex(0.15); int x_t = 4, y_t = 4;//func=poly
        boolean def = (args.length == 0); ComplexFractalConfig fccfg = new ComplexFractalConfig(0, 0, 0); if (!def) {
            try {
                fccfg = ConfigReader.getComplexFractalConfigFromFile(new File(args[0]));
            } catch (Exception e) {x_t = Integer.valueOf(args[0]); y_t = Integer.valueOf(args[1]); def = true;}
        } long inittime = System.currentTimeMillis(); ComplexFractalGenerator jgen;
        ComplexFractalParams jgenParams = null; if (def) {
            jgenParams = new ComplexFractalParams(new ComplexFractalInitParams(resx, resy, zoom, zoompow, baseprec, fracmode, func, consts, variableCode, tolerance, cfg, switch_rate, trap), null);
            if (constant != null) { jgenParams.runParams = new ComplexFractalRunParams(iter, escrad, constant);} else {
                jgenParams.runParams = new ComplexFractalRunParams(iter, escrad);
            } jgenParams.x_threads = x_t; jgenParams.y_threads = y_t;
            jgen = new ComplexFractalGenerator(jgenParams, new DesktopProgressPublisher());
        } else {jgen = new ComplexFractalGenerator(fccfg.getParams()[0], new DesktopProgressPublisher());}
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        if (def) {
            if (jgenParams.useThreadedGenerator()) {
                ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(jgen);
                threaded.generate();
            } else {jgen.generate(jgenParams);}
        } else {jgen.generate(fccfg.getParams()[0]);} long gentime = System.currentTimeMillis();
        System.out.println("Generating fractal took:" + ((double) (gentime - starttime) / 60000) + "mins");
        File pic = new File("D:/Fractal.png"); try {
            ImageIO.write(ImageConverter.toImage(jgen.getArgand()/*.getPostProcessed(ImageData.PostProcessMode.INTERPOLATED_AVERAGE, jgen.getNormalized_escapes(), jgen.getColor().isByParts())*/), "png", pic);
        } catch (Exception e) {e.printStackTrace();} long endtime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endtime - gentime) + "ms");
    }
    static int rgb(int r, int g, int b) {return ColorConfig.toRGB(r, g, b);}
}