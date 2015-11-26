package in.tamchow.fractal;

import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.color.ColorMode;
import in.tamchow.fractal.config.fractalconfig.FractalConfig;
import in.tamchow.fractal.math.complex.Complex;
import in.tamchow.fractal.platform_tools.ImageConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Main class, handles CMDLINE input.
 */
public class Main {
    public static void main(String[] args) {
        String func = "( z ^ 3 ) + ( d * z ) + c", variableCode = "z", poly = "{1,z,3};+;{d,z,1};+;{c,z,0}";
        String[][] consts = {{"c", "-0.8,+0.156i"}, {"d", "-0.7198,+0.911i"}};
        int resx = 401, resy = 401, zoom = 10, zoompow = 0, baseprec = 150, colmode = ColorMode.COLOR_NEWTON_1, numcol = 32, coldens = 125, fracmode = FractalGenerator.MODE_NEWTON, iter = 8;
        double bound = 2.0, escrad = 2.0, tolerance = 1e-1;
        Complex constant = null;
        func = poly;
        boolean fromFile = false;
        FractalConfig fccfg = new FractalConfig(0, 0, 0);
        if (args.length > 1) {
            func = args[0];
            consts[0][0] = args[1].substring(0, args[1].indexOf(':'));
            consts[0][1] = args[1].substring(args[1].indexOf(':') + 1, args[1].length());
            variableCode = args[2];
            resx = Integer.valueOf(args[3]);
            resy = Integer.valueOf(args[4]);
            zoom = Integer.valueOf(args[5]);
            zoompow = Integer.valueOf(args[6]);
            baseprec = Integer.valueOf(args[7]);
            colmode = Integer.valueOf(args[8]);
            numcol = Integer.valueOf(args[9]);
            coldens = Integer.valueOf(args[10]);
            fracmode = Integer.valueOf(args[11]);
            bound = Double.valueOf(args[12]);
            iter = Integer.valueOf(args[13]);
            escrad = Double.valueOf(args[14]);
            if (args.length >= 16) {
                tolerance = Double.valueOf(args[15]);
            }
            if (args.length == 17) {
                constant = new Complex(args[16]);
            }
            fromFile = false;
        } else if (args.length == 1) {
            try {
                fccfg = ConfigReader.getFractalConfigFromFile(new File(args[0]));
                fromFile = true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        long inittime = System.currentTimeMillis();
        FractalGenerator jgen;
        if (!fromFile) {
            jgen = new FractalGenerator(resx, resy, zoom, zoompow, baseprec, colmode, numcol, coldens, fracmode, bound, func, consts, variableCode, tolerance);
        } else {
            jgen = new FractalGenerator(fccfg.getParams()[0]);
        }
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        if (!fromFile) {
            if (constant != null) {
                jgen.generate(iter, escrad, constant);
            } else {
                jgen.generate(iter, escrad);
            }
        } else {
            jgen.generate(fccfg.getParams()[0]);
        }

        long gentime = System.currentTimeMillis();
        System.out.println("Generating fractal took:" + ((double) (gentime - starttime) / 60000) + "mins");
        File pic = new File("D:/Fractal.jpg");
        File postpic = new File("D:/Fractal_processed.jpg");
        try {
            ImageIO.write(ImageConverter.toImage(jgen.getArgand()), "jpg", pic);
            ImageIO.write(ImageConverter.toImage(jgen.getArgand().getColorAveraged()), "jpg", postpic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endtime = System.currentTimeMillis();
        System.out.println("Writing image took:" + (endtime - gentime) + "ms");
        /*ImageData[] imgs={new ImageData("D:/Fractal.png")};
        int[] trans={-1};
        int wait=1;
        ImageConfig ic=new ImageConfig(5,40,imgs,trans,wait);
        ImageDisplay.show(ic,"Images");*/
        /*Random random=new Random();
        jgen.zoom(jgen.toCooordinates(jgen.boundary_points.get(random.nextInt(jgen.boundary_points.size())))[0],jgen.toCooordinates(jgen.boundary_points.get(random.nextInt(jgen.boundary_points.size())))[1],2);
        //jgen.zoom(jgen.getCenter_x(), jgen.getCenter_y(), 1);
        jgen.generate(128);
        System.out.println(jgen.boundary_points.get(0));
        try {
            ImageIO.write(Image_ImageData.toImage(jgen.getArgand()), "png", zoompic);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
