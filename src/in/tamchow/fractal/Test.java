package in.tamchow.fractal;
import in.tamchow.fractal.color.ColorConfig;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
/**
 * Test class, handles CMDLINE input.
 */
public class Test {
    public static void main(String[] args) {
        String func = "z ^ 4 + 1", variableCode = "z", poly = "{1:z:4};+;{1:z:0}";
        String[][] consts = {{"c", "-0.1,+0.651i"}, {"d", "-0.7198,+0.911i"}, {"e", "-0.8,+0.156i"}, {"f", "-0.5,+0.25i"}};
        int resx = 801, resy = 801, fracmode = ComplexFractalGenerator.MODE_JULIA, iter = 32;
        double escrad = 2, tolerance = 1e-10, zoom = 10, zoompow = 0, baseprec = 300;
        //ColorConfig cfg = new ColorConfig(Colors.CALCULATIONS.STRIPE_AVERAGE, 8, 65536,0xff0000,0xfff000,true,true);
        ColorConfig cfg = new ColorConfig(Colors.CALCULATIONS.STRIPE_AVERAGE, 8, 256, false, true);
        Complex constant = null;//new Complex("1.0,+0.0i");//func = poly;
        boolean def = (args.length == 0); ComplexFractalConfig fccfg = new ComplexFractalConfig(0, 0, 0); if (!def) {
            try {
                fccfg = ConfigReader.getComplexFractalConfigFromFile(new File(args[0]));
            } catch (IOException ioe) {ioe.printStackTrace();}
        } long inittime = System.currentTimeMillis(); ComplexFractalGenerator jgen; if (def) {
            jgen = new ComplexFractalGenerator(resx, resy, zoom, zoompow, baseprec, fracmode, func, consts, variableCode, tolerance, cfg);
        } else {jgen = new ComplexFractalGenerator(fccfg.getParams()[0]);} long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        if (def) {
            //ThreadedComplexFractalGenerator tg=new ThreadedComplexFractalGenerator(2, 2, jgen, iter, escrad, constant);
            //tg.generate();
            if (constant != null) {jgen.generate(iter, escrad, constant);} else {jgen.generate(iter, escrad);}
        } else {jgen.generate(fccfg.getParams()[0]);} long gentime = System.currentTimeMillis();
        System.out.println("Generating fractal took:" + ((double) (gentime - starttime) / 60000) + "mins");
        File pic = new File("D:/Fractal.jpg"); File postpic = new File("D:/Fractal_processed.jpg"); try {
            ImageIO.write(ImageConverter.toImage(jgen.getArgand()), "jpg", pic);
            //ImageIO.write(ImageConverter.toImage(jgen.getArgand().getColorAveraged()), "jpg", postpic);
        } catch (Exception e) {e.printStackTrace();} long endtime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endtime - gentime) + "ms");
    }
}