package in.tamchow.fractal;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalConfig;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.fractals.IFS.IFSGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.misc.EncryptDecryptFile;
import in.tamchow.fractal.misc.PBrainer;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;
import in.tamchow.fractal.platform_tools.ImageDisplay;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
/**
 * Production Main Class: Handles Images, Complex and IFS Fractals. Max. of 2 required arguments.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {System.err.println("Nothing to do."); System.exit(1);}
        if (args[0].equalsIgnoreCase("PBrain")) {
            String[] modArgs = new String[args.length - 1]; System.arraycopy(args, 1, modArgs, 0, modArgs.length);
            PBrainer.main(modArgs);
        } else if (args[0].equalsIgnoreCase("encrypt") || args[0].equalsIgnoreCase("decrypt")) {
            String[] modArgs = new String[args.length - 1]; System.arraycopy(args, 1, modArgs, 0, modArgs.length);
            try {EncryptDecryptFile.main(modArgs);} catch (IOException ioe) {
                System.err.println("I/O Error: " + ioe.getMessage());
            }
        } File input = new File(args[0]); if (!input.exists()) {
            System.err.println("Specified Input File doesn't exist. Please check the input path."); System.exit(2);
        } try {
            if (ConfigReader.isFileImageConfig(input)) {
                ImageConfig ic = ConfigReader.getImageConfigFromFile(input);
                ImageDisplay.show(ic, "Images from config file:" + input.getCanonicalPath());
            } else if (ConfigReader.isFileComplexFractalConfig(input)) {
                if (args.length == 1) {System.err.println("No output directory specified"); System.exit(3);}
                if (args.length == 3) {
                    if (args[2].equalsIgnoreCase("-v")) {
                        ImageDisplay.show(ConfigReader.getComplexFractalConfigFromFile(input), "Fractal");
                    }
                }
                ComplexFractalConfig cfg = ConfigReader.getComplexFractalConfigFromFile(input);
                for (int i = 0; i < cfg.getParams().length; i++) {
                    ComplexFractalParams params = cfg.getParams()[i];
                    ComplexFractalGenerator generator = new ComplexFractalGenerator(params, new DesktopProgressPublisher());
                    if (params.useThreadedGenerator()) {
                        ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(generator, params);
                        threaded.generate();
                    } else {
                        generator.generate(params);
                    } File outputFile = new File(args[1] + "/Fractal_" + i + ".jpg");
                    if (params.getPostprocessMode() != -1) {
                        ImageIO.write(ImageConverter.toImage(generator.getArgand().getPostProcessed(params.getPostprocessMode(), generator.getNormalized_escapes(), generator.getColor().isByParts())), "jpg", outputFile);
                    } else {
                        ImageIO.write(ImageConverter.toImage(generator.getArgand()), "jpg", outputFile);
                    }
                }
            } else if (ConfigReader.isFileIFSFractalConfig(input)) {
                if (args.length == 1) {System.err.println("No output directory specified"); System.exit(3);}
                IFSFractalConfig cfg = ConfigReader.getIFSFractalConfigFromFile(input);
                for (int i = 0; i < cfg.getParams().length; i++) {
                    IFSFractalParams params = cfg.getParams()[i];
                    IFSGenerator generator = new IFSGenerator(params, new DesktopProgressPublisher());
                    generator.generate(); File outputFile = new File(args[1] + "/Fractal_" + i + ".jpg");
                    ImageIO.write(ImageConverter.toImage(generator.getPlane()), "jpg", outputFile);
                }
            }
        } catch (IOException ioe) {System.out.println("I/O Error: " + ioe.getMessage());}
    }
}