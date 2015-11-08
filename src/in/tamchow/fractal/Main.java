package in.tamchow.fractal;

import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.FractalConfig;
import in.tamchow.fractal.imgutils.ColorMode;
import in.tamchow.fractal.platform_tools.Image_ImageData;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Main class, handles CMDLINE input.
 */
public class Main {
    public static void main(String[] args) {
        String func = "z ^ 2 + c";
        String[][] consts = {{"c", "-0.8,+0.156i"}};
        int resx = 401, resy = 401, zoom = 10, zoompow = 0, baseprec = 150, colmode = ColorMode.COLOR_GRAYSCALE, numcol = 32, coldens = 256, fracmode = FractalGenerator.MODE_JULIA, iter = 128;
        double bound = 2.0, escrad = 2.0;
        boolean fromFile = false;
        FractalConfig fccfg = new FractalConfig(0, 0, 0);
        if (args.length > 1) {
            func = args[0];
            consts[0][0] = args[1].substring(0, args[1].indexOf(':'));
            consts[0][1] = args[1].substring(args[1].indexOf(':') + 1, args[1].length());
            resx = Integer.valueOf(args[2]);
            resy = Integer.valueOf(args[3]);
            zoom = Integer.valueOf(args[4]);
            zoompow = Integer.valueOf(args[5]);
            baseprec = Integer.valueOf(args[6]);
            colmode = Integer.valueOf(args[7]);
            numcol = Integer.valueOf(args[8]);
            coldens = Integer.valueOf(args[9]);
            fracmode = Integer.valueOf(args[10]);
            bound = Double.valueOf(args[11]);
            iter = Integer.valueOf(args[12]);
            escrad = Double.valueOf(args[13]);
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
            jgen = new FractalGenerator(resx, resy, zoom, zoompow, baseprec, colmode, numcol, coldens, fracmode, bound, func, consts);
        } else {
            jgen = new FractalGenerator(fccfg.getParams()[0]);
        }
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        if (!fromFile) {
            jgen.generate(iter, escrad);
        } else {
            jgen.generate(fccfg.getParams()[0]);
        }
        long gentime = System.currentTimeMillis();
        System.out.println("Generating fractal took:" + ((double) (gentime - starttime) / 60000) + "mins");
        //System.out.println(jgen.boundary_points.get(0));
        File pic = new File("D:/Fractal.jpg");
        //File zoompic = new File("D:/Fractal_zoom.png");
        try {
            ImageIO.write(Image_ImageData.toImage(jgen.getArgand()), "jpg", pic);
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
