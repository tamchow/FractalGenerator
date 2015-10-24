package in.tamchow.fractal;

import in.tamchow.fractal.imgutils.ColorMode;
import in.tamchow.fractal.platform_tools.Image_ImageData;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * Test class, to be removed once Swing app for fractal display is completed
 */
public class Main {
    public static void main(String[] args) {
        String func = "z ^ 2 + c";
        String[][] consts = {{"c", "-0.8,+0.156i"}};
        long inittime = System.currentTimeMillis();
        FractalGenerator jgen = new FractalGenerator(1921, 1081, 10, 0, 540, ColorMode.COLOR_DIVIDE, 32, 256, FractalGenerator.MODE_JULIA, 2, func, consts);
        long starttime = System.currentTimeMillis();
        System.out.println("Initiating fractal took:" + (starttime - inittime) + "ms");
        jgen.generate(32, 2.0, 2.0);
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
