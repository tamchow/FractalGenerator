package in.tamchow.fractal;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.color.InterpolationType;
import in.tamchow.fractal.config.BatchContainer;
import in.tamchow.fractal.config.ConfigReader;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.imageconfig.ImageParams;
import in.tamchow.fractal.fractals.IFS.IFSGenerator;
import in.tamchow.fractal.fractals.IFS.ThreadedIFSGenerator;
import in.tamchow.fractal.fractals.complex.ComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.ThreadedComplexFractalGenerator;
import in.tamchow.fractal.fractals.complex.complexbrot.ComplexBrotFractalGenerator;
import in.tamchow.fractal.fractals.complex.complexbrot.ThreadedComplexBrotFractalGenerator;
import in.tamchow.fractal.fractals.l_system.LSFractalGenerator;
import in.tamchow.fractal.graphics.containers.Animation;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.misc.RC4Utility.EncryptDecryptFile;
import in.tamchow.fractal.misc.bs.BF2Java;
import in.tamchow.fractal.misc.bs.BrainSext;
import in.tamchow.fractal.misc.mathstuff.DoughNut;
import in.tamchow.fractal.misc.primes.PrimeCounter;
import in.tamchow.fractal.platform_tools.DesktopProgressPublisher;
import in.tamchow.fractal.platform_tools.ImageConverter;
import in.tamchow.fractal.platform_tools.ImageDisplay;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Production Main Class: Handles Images, Complex, IFS and LS Fractals.
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
                    }
                    if (args.length > 1 && args[0].equalsIgnoreCase("-v")) {
                        BatchContainer<ComplexFractalParams> cfconfiglist = new BatchContainer<>();
                        for (int i = 1; i < args.length; ++i) {
                            cfconfiglist.addItem(ConfigReader.getComplexParamFromFile(new File(args[i])));
                        }
                        ImageDisplay.show(cfconfiglist, "Fractal");
                    } else {
                        for (int i = 1; i < args.length; i++) {
                            ComplexFractalParams params = ConfigReader.getComplexParamFromFile(new File(args[i]));
                            @NotNull ComplexFractalGenerator generator = new ComplexFractalGenerator(params,
                                    new DesktopProgressPublisher());
                            @NotNull ThreadedComplexFractalGenerator threaded = new ThreadedComplexFractalGenerator(
                                    generator);
                            threaded.generate();
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (MathUtils.isAnyOf(generator.getColor().getMode(),
                                    Colors.MODE.ASCII_ART_CHARACTER, Colors.MODE.ASCII_ART_NUMERIC)) {
                                String ascii = generator.createASCIIArt();
                                try {
                                    BufferedWriter writer = new BufferedWriter(new FileWriter(args[1] +
                                            "/Fractal_" + i + ".txt", false));
                                    writer.write(ascii);
                                    writer.flush();
                                    writer.close();
                                } catch (IOException ignored) {
                                    ignored.printStackTrace();
                                }
                                if (params.postProcessMode == PixelContainer.PostProcessMode.TEXT_TO_IMAGE) {
                                    ImageIO.write(ImageConverter.drawTextToImage(ascii, Font.MONOSPACED, Font.PLAIN,
                                            0xff000000, 0xffffffff, 10, 0, 0),
                                            "png", outputFile);
                                }
                            } else {
                                if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                    ImageIO.write(ImageConverter.toImage(generator.getArgand().getPostProcessed(
                                            params.getPostProcessMode(), generator.getNormalizedEscapes(),
                                            generator.getColor().getByParts(),
                                            generator.getColor().getInterpolationType(),
                                            generator.getColor().isGammaCorrection())), "png", outputFile);
                                } else {
                                    ImageIO.write(ImageConverter.toImage(generator.getArgand()),
                                            "png", outputFile);
                                }
                            }
                        }
                    }
                } else if (ConfigReader.isFileComplexBrotFractalConfig(input)) {
                    if (args.length == 1) {
                        System.err.println("No output directory specified for batch mode");
                        System.exit(3);
                    }
                    for (int i = 1; i < args.length; i++) {
                        ComplexBrotFractalParams params = ConfigReader.getComplexBrotParamFromFile(new File(args[i]));
                        @NotNull ComplexBrotFractalGenerator generator = new ComplexBrotFractalGenerator(params,
                                new DesktopProgressPublisher());
                        @NotNull ThreadedComplexBrotFractalGenerator threaded = new ThreadedComplexBrotFractalGenerator(
                                generator);
                        threaded.generate();
                        @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                        if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                            ImageIO.write(ImageConverter.toImage(generator.getPlane().getPostProcessed(
                                    params.getPostProcessMode(), null, 0, InterpolationType.LINEAR, false)),
                                    "png", outputFile);
                        } else {
                            ImageIO.write(ImageConverter.toImage(generator.getPlane()), "png", outputFile);
                        }
                    }
                } else if (ConfigReader.isFileIFSFractalConfig(input)) {
                    if (args.length == 1) {
                        System.err.println("No output directory specified");
                        System.exit(3);
                    }
                    for (int i = 1; i < args.length; i++) {
                        IFSFractalParams params = ConfigReader.getIFSParamFromFile(new File(args[i]));
                        @NotNull IFSGenerator generator = new IFSGenerator(params, new DesktopProgressPublisher());
                        @NotNull ThreadedIFSGenerator threaded = new ThreadedIFSGenerator(generator);
                        threaded.generate();
                        if (params.isAnimated()) {
                            Animation frames = generator.getAnimation();
                            @NotNull File animationMetaData = new File(args[1] +
                                    "/Fractal_" + i + "/animation.cfg");
                            @NotNull BufferedWriter writer = new BufferedWriter(new FileWriter(animationMetaData));
                            writer.write(frames.sizeDataString() + "");
                            writer.flush();
                            writer.close();
                            for (int j = 0; j < frames.getNumFrames(); j++) {
                                @NotNull File outputFile = new File(args[1] +
                                        "/Fractal_" + i + "/Frame_" + j + ".png");
                                if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                    ImageIO.write(ImageConverter.toImage(generator.getPlane().getPostProcessed(
                                            params.getPostProcessMode(), null, 0, InterpolationType.LINEAR, false)),
                                            "png", outputFile);
                                } else {
                                    ImageIO.write(ImageConverter.toImage(generator.getPlane()),
                                            "png", outputFile);
                                }
                            }
                        } else {
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                ImageIO.write(ImageConverter.toImage(generator.getPlane().getPostProcessed(
                                        params.getPostProcessMode(), null, 0, InterpolationType.LINEAR, false)),
                                        "png", outputFile);
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
                    for (int i = 1; i < args.length; i++) {
                        LSFractalParams params = ConfigReader.getLSParamFromFile(new File(args[i]));
                        @NotNull LSFractalGenerator generator = new LSFractalGenerator(params,
                                new DesktopProgressPublisher());
                        if (params.getFps() > 0) {
                            @NotNull Animation frames = generator.drawStatesAsAnimation();
                            @NotNull File animationMetaData = new File(args[1] +
                                    "/Fractal_" + i + "/animation.cfg");
                            @NotNull BufferedWriter writer = new BufferedWriter(new FileWriter(animationMetaData));
                            writer.write(frames + "");
                            writer.flush();
                            writer.close();
                            for (int j = 0; j < frames.getNumFrames(); j++) {
                                @NotNull File outputFile = new File(args[1] +
                                        "/Fractal_" + i + "/Frame_" + j + ".png");
                                if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                    ImageIO.write(ImageConverter.toImage(frames.getFrame(j).getPostProcessed(
                                            params.getPostProcessMode(), null, 0, InterpolationType.LINEAR, false)),
                                            "png", outputFile);
                                } else {
                                    ImageIO.write(ImageConverter.toImage(frames.getFrame(j)), "png",
                                            outputFile);
                                }
                            }
                        } else {
                            generator.generate();
                            generator.drawState(generator.getParams().getDepth() - 1);
                            @NotNull File outputFile = new File(args[1] + "/Fractal_" + i + ".png");
                            if (params.getPostProcessMode() != PixelContainer.PostProcessMode.NONE) {
                                ImageIO.write(ImageConverter.toImage(generator.getCanvas().getPostProcessed(
                                        params.getPostProcessMode(), null, 0, InterpolationType.LINEAR, false)),
                                        "png", outputFile);
                            } else {
                                ImageIO.write(ImageConverter.toImage(generator.getCanvas()),
                                        "png", outputFile);
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                System.err.println("I/O Error: " + ioe.getMessage());
            }
        }
    }
}