package in.tamchow.fractal;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
/**
 * Test class, handles CMDLINE input.
 */
public class Test {
    public static void main(String[] args) {
        String func = "f * ( sin z )", variableCode = "z", poly = "{1:z:4};-;{1:z:0}", poly2 = "{f:z:0};sin;{1:z:1}";
        String[][] consts = {{"c", "-0.1,+0.651i"}, {"d", "-0.7198,+0.911i"}, {"e", "-0.8,+0.156i"}, {"f", "-0.5,+0.25i"}, {"f", "1,+0.3i"}};
        int resx = 1920, resy = 1080, fracmode = ComplexFractalGenerator.MODE_NEWTON, iter = 16;
        double escrad = 50, tolerance = 1e-15, zoom = 10, zoompow = 0, baseprec = 540;
        ColorConfig cfg = new ColorConfig(Colors.CALCULATIONS.COLOR_NEWTON_STRIPES, 4, 4, true, true);
        cfg.setExponentialSmoothing(true);
        cfg.setColor_density(cfg.calculateColorDensity());
        //cfg.setPalette(new int[]{rgb(66, 30, 15), rgb(25, 7, 26), rgb(9, 1, 47), rgb(4, 4, 73), rgb(0, 7, 100), rgb(12, 44, 138), rgb(24,82,177),rgb(57,125,209), rgb(134,181,229), rgb(211,236,248), rgb(241,233,191), rgb(248,201,95), rgb(255,170,0), rgb(204,128,0), rgb(153,87,0), rgb(106,52,3)},false);
        //cfg.createSmoothPalette(new int[]{rgb(0, 7, 100), rgb(32, 107, 203), rgb(237, 255, 255), rgb(255, 170, 0), rgb(0, 2, 0)}, new double[]{0.0, 0.16, 0.42, 0.6425, 0.8575}, true);
        //cfg.setPalette(new int[]{0xff0000, 0x00ff00, 0x0000ff, 0xfff000}, false);
        cfg.createSmoothPalette(new int[]{0xff0000, 0x00ff00, 0x0000ff, 0xfff000}, new double[]{0.2, 0.4, 0.6, 0.8}, true);
        cfg.setColor_density(cfg.calculateColorDensity());
        Complex constant = null;//new Complex("1.0,+0.0i");
        func = poly;
        boolean def = (args.length == 0); ComplexFractalConfig fccfg = new ComplexFractalConfig(0, 0, 0); if (!def) {
            try {
                fccfg = ConfigReader.getComplexFractalConfigFromFile(new File(args[0]));
            } catch (IOException ioe) {ioe.printStackTrace();}
        } long inittime = System.currentTimeMillis(); ComplexFractalGenerator jgen; if (def) {
            jgen = new ComplexFractalGenerator(resx, resy, zoom, zoompow, baseprec, fracmode, func, consts, variableCode, tolerance, cfg, new DesktopProgressPublisher());
        } else {jgen = new ComplexFractalGenerator(fccfg.getParams()[0], new DesktopProgressPublisher());}
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        if (def) {
            //ThreadedComplexFractalGenerator tg=new ThreadedComplexFractalGenerator(2, 2, jgen, iter, escrad, constant);
            //tg.generate();
            if (constant != null) {jgen.generate(iter, escrad, constant);} else {
                //jgen.zoom(Matrix.fromString("[[0.8];[0]]"),1);
                jgen.generate(iter, escrad);
            }
        } else {jgen.generate(fccfg.getParams()[0]);} long gentime = System.currentTimeMillis();
        System.out.println("Generating fractal took:" + ((double) (gentime - starttime) / 60000) + "mins");
        File pic = new File("D:/Fractal.jpg"); try {
            ImageIO.write(ImageConverter.toImage(jgen.getArgand().getPostProcessed(ImageData.INTERPOLATED_AVERAGE, jgen.getNormalized_escapes(), jgen.getColor().isByParts())), "jpg", pic);
        } catch (Exception e) {e.printStackTrace();} long endtime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endtime - gentime) + "ms");
    }
    static int rgb(int r, int g, int b) {return ColorConfig.toRGB(r, g, b);}
}