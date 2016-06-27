package in.tamchow.fractal;
import in.tamchow.fractal.config.BatchContainer;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.imageconfig.ImageParams;
import in.tamchow.fractal.fractals.IFS.IFSGenerator;
import in.tamchow.fractal.fractals.IFS.ThreadedIFSGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.fractals.complexbrot.ComplexBrotFractalGenerator;
import in.tamchow.fractal.fractals.complexbrot.ThreadedComplexBrotFractalGenerator;
import in.tamchow.fractal.fractals.l_system.LSFractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.misc.RC4Utility.EncryptDecryptFile;
import in.tamchow.fractal.misc.bs.BF2Java;
import in.tamchow.fractal.misc.bs.BrainSext;
import in.tamchow.fractal.misc.mathstuff.DoughNut;
import in.tamchow.fractal.misc.primes.PrimeCounter;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;
import in.tamchow.fractal.platform_tools.ImageDisplay;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Production Main Class: Handles Images, Complex and IFS Fractals.
 * Max. of 2 required arguments, not including switches or flags
 */
public class Main {
    private static String[] modArgs(String[] args) {
        @NotNull String[] modArgs = new String[args.length - 1];
        System.arraycopy(args, 1, modArgs, 0, modArgs.length);
        return modArgs;
    }
    public static void main(@NotNull String[] args) {
        if (args.length == 0) {
            System.err.println("Nothing to do.");
            System.exit(1);
        }
        if (args[0].equalsIgnoreCase("/BS")) {
            BrainSext.main(modArgs(args));
        } else if (args[0].equalsIgnoreCase("/BF2J")) {
            BF2Java.main(modArgs(args));
        } else if (args[0].equalsIgnoreCase("/primecount") || args[0].equalsIgnoreCase("/pc")) {
            PrimeCounter.main(modArgs(args));
        } else if (args[0].equalsIgnoreCase("/encrypt") || args[0].equalsIgnoreCase("/decrypt")) {
            try {
                EncryptDecryptFile.main(modArgs(args));
            } catch (IOException ioe) {
                System.err.println("I/O Error: " + ioe.getMessage());
            }
        } else if (args[0].equalsIgnoreCase("-t") || args[0].equalsIgnoreCase("-test")) {
            Test.main(modArgs(args));
        } else if (args[0].equalsIgnoreCase("/dn") || args[0].equalsIgnoreCase("/doughnut")) {
            DoughNut.main(modArgs(args));
        } else {
            @NotNull File input = new File(args[0]);
            if (!input.exists()) {
                System.err.println("Specified Input File doesn't exist. Please check the input path.");
                System.exit(2);
            }
            try {
                if (ConfigReader.isFileImageConfig(input)) {
                    @Nullable BatchContainer<ImageParams> ic = ConfigReader.getImageConfigFromFile(input);
                    ImageDisplay.show(ic, "Images from config file:" + input.getCanonicalPath());
                } else if (ConfigReader.isFileComplexFractalConfig(input)) {
                    if (args.length == 1) {
                        System.err.println("No output directory specified for batch mode");
                        System.exit(3);
                    } else if (args.length == 2 && args[1].equalsIgnoreCase("-v")) {
                        ImageDisplay.show(ConfigReader.getComplexFractalConfigFromFile(input), "Fractal");
                    } else {
                        @Nullable BatchContainer<ComplexFractalParams> cfg = ConfigReader.getComplexFractalConfigFromFile(input);
                        for (int i = 0; i < cfg.size(); i++) {
                            ComplexFractalParams params = cfg.getItem(i);
                            @NotNull ComplexFractalGenerator generator = new ComplexFractalGenerator(params, new DesktopProgressPublisher());
                            if (params.useThreadedGenerator()) {
                                @NotNull ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(generator);
                                threaded.generate();
                            } else {
                                generator.generate();
                            }
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                ImageIO.write(ImageConverter.toImage(generator.getArgand().getPostProcessed(params.getPostProcessMode(), generator.getNormalized_escapes(), generator.getColor().getByParts())), "png", outputFile);
                            } else {
                                ImageIO.write(ImageConverter.toImage(generator.getArgand()), "png", outputFile);
                            }
                        }
                    }
                } else if (ConfigReader.isFileComplexBrotFractalConfig(input)) {
                    if (args.length == 1) {
                        System.err.println("No output directory specified for batch mode");
                        System.exit(3);
                    } else {
                        @Nullable BatchContainer<ComplexBrotFractalParams> cfg = ConfigReader.getComplexBrotFractalConfigFromFile(input);
                        for (int i = 0; i < cfg.size(); i++) {
                            ComplexBrotFractalParams params = cfg.getItem(i);
                            @NotNull ComplexBrotFractalGenerator generator = new ComplexBrotFractalGenerator(params, new DesktopProgressPublisher());
                            if (params.useThreadedGenerator()) {
                                @NotNull ThreadedComplexBrotFractalGenerator threaded = new ThreadedComplexBrotFractalGenerator(generator);
                                threaded.generate();
                            } else {
                                generator.generate();
                            }
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                ImageIO.write(ImageConverter.toImage(generator.getPlane().getPostProcessed(params.getPostProcessMode(), null, 0)), "png", outputFile);
                            } else {
                                ImageIO.write(ImageConverter.toImage(generator.getPlane()), "png", outputFile);
                            }
                        }
                    }
                } else if (ConfigReader.isFileIFSFractalConfig(input)) {
                    if (args.length == 1) {
                        System.err.println("No output directory specified");
                        System.exit(3);
                    }
                    @Nullable BatchContainer<IFSFractalParams> cfg = ConfigReader.getIFSFractalConfigFromFile(input);
                    for (int i = 0; i < cfg.size(); i++) {
                        IFSFractalParams params = cfg.getItem(i);
                        @NotNull IFSGenerator generator = new IFSGenerator(params, new DesktopProgressPublisher());
                        if (params.useThreadedGenerator()) {
                            @NotNull ThreadedIFSGenerator threaded = new ThreadedIFSGenerator(generator);
                            threaded.generate();
                        } else {
                            generator.generate();
                        }
                        if (params.isAnimated()) {
                            Animation frames = generator.getAnimation();
                            @NotNull File animationMetaData = new File(args[1] + "/Fractal_" + i + "/animation.cfg");
                            @NotNull BufferedWriter writer = new BufferedWriter(new FileWriter(animationMetaData));
                            writer.write(frames.sizeDataString() + "");
                            writer.flush();
                            writer.close();
                            for (int j = 0; j < frames.getNumFrames(); j++) {
                                @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + "/Frame_" + j + ".png");
                                if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                    ImageIO.write(ImageConverter.toImage(generator.getPlane().getPostProcessed(params.getPostProcessMode(), null, 0)), "png", outputFile);
                                } else {
                                    ImageIO.write(ImageConverter.toImage(generator.getPlane()), "png", outputFile);
                                }
                            }
                        } else {
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                ImageIO.write(ImageConverter.toImage(generator.getPlane().getPostProcessed(params.getPostProcessMode(), null, 0)), "png", outputFile);
                            } else {
                                ImageIO.write(ImageConverter.toImage(generator.getPlane()), "png", outputFile);
                            }
                        }
                    }
                } else if (ConfigReader.isFileLSFractalConfig(input)) {
                    if (args.length == 1) {
                        System.err.println("No output directory specified");
                        System.exit(3);
                    }
                    @Nullable BatchContainer<LSFractalParams> cfg = ConfigReader.getLSFractalConfigFromFile(input);
                    for (int i = 0; i < cfg.size(); i++) {
                        LSFractalParams params = cfg.getItem(i);
                        @NotNull LSFractalGenerator generator = new LSFractalGenerator(params, new DesktopProgressPublisher());
                        if (params.getFps() > 0) {
                            @NotNull Animation frames = generator.drawStatesAsAnimation();
                            @NotNull File animationMetaData = new File(args[1] + "/Fractal_" + i + "/animation.cfg");
                            @NotNull BufferedWriter writer = new BufferedWriter(new FileWriter(animationMetaData));
                            writer.write(frames + "");
                            writer.flush();
                            writer.close();
                            for (int j = 0; j < frames.getNumFrames(); j++) {
                                @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + "/Frame_" + j + ".png");
                                if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                    ImageIO.write(ImageConverter.toImage(frames.getFrame(j).getPostProcessed(params.getPostProcessMode(), null, 0)), "png", outputFile);
                                } else {
                                    ImageIO.write(ImageConverter.toImage(frames.getFrame(j)), "png", outputFile);
                                }
                            }
                        } else {
                            generator.generate();
                            generator.drawState(generator.getParams().getDepth() - 1);
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                ImageIO.write(ImageConverter.toImage(generator.getCanvas().getPostProcessed(params.getPostProcessMode(), null, 0)), "png", outputFile);
                            } else {
                                ImageIO.write(ImageConverter.toImage(generator.getCanvas()), "png", outputFile);
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                System.out.println("I/O Error: " + ioe.getMessage());
            }
        }
    }
}