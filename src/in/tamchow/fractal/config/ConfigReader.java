package in.tamchow.fractal.config;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.imageconfig.ImageParams;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static in.tamchow.fractal.config.Strings.BLOCKS.*;
import static in.tamchow.fractal.config.Strings.COMMENT;
import static in.tamchow.fractal.config.Strings.DECLARATIONS.*;
/**
 * Reads the configuration of fractals or images from a file
 */
public class ConfigReader {
    public static boolean isFileImageConfig(@NotNull File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals(IMAGE);
    }
    public static boolean isFileComplexFractalConfig(@NotNull File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals(COMPLEX);
    }
    public static boolean isFileComplexBrotFractalConfig(@NotNull File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals(COMPLEXBROT);
    }
    public static boolean isFileIFSFractalConfig(@NotNull File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals(IFS);
    }
    public static boolean isFileLSFractalConfig(@NotNull File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals(LS);
    }
    private static List<String> prepareBatchLines(@NotNull File cfgFile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(cfgFile);
        @NotNull List<String> lines = new ArrayList<>();
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        return lines;
    }
    public static BatchContainer<ImageParams> getImageConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        List<String> lines = prepareBatchLines(cfgfile);
        if (!lines.get(0).equals(IMAGE)) {
            return null;
        }
        lines.remove(0);
        @NotNull BatchContainer<ImageParams> imageConfig = new BatchContainer<>();
        for (String line : lines) {
            ImageParams imageParams = new ImageParams();
            imageParams.fromString(line);
            imageConfig.addItem(imageParams);
        }
        return imageConfig;
    }
    @NotNull
    public static ComplexFractalParams getComplexParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull ArrayList<String> lines = new ArrayList<>(prepareBatchLines(paramfile)), linesBk = new ArrayList<>(lines);
        @Nullable String thread_data = null, post_process_mode = null, switch_rate = null, trap_point = null, trap_line = null, oldvariablecode = null;
        for (String line : lines) {
            thread_data = processCommand(linesBk, line, THREADS);
            switch_rate = processCommand(linesBk, line, SWITCH_RATE);
            post_process_mode = processCommand(linesBk, line, POSTPROCESSING);
            trap_point = processCommand(linesBk, line, TRAP_POINT);
            trap_line = processCommand(linesBk, line, TRAP_LINE);
            oldvariablecode = processCommand(linesBk, line, OLD_VARIABLE_CODE);
        }
        lines = linesBk;
        @Nullable List<String> zooms = null;
        if (lines.indexOf(ZOOMS) >= 0) {
            zooms = lines.subList(lines.indexOf(ZOOMS) + 1, lines.indexOf(ENDZOOMS));
        }
        @NotNull List<String> initConfig = lines.subList(lines.indexOf(INIT) + 1, lines.indexOf(ENDINIT));
        @NotNull List<String> runConfig = lines.subList(lines.indexOf(RUN) + 1, lines.indexOf(ENDRUN));
        @NotNull ComplexFractalParams complexFractalParams = new ComplexFractalParams();
        complexFractalParams.initParams.fromString(initConfig.toArray(new String[initConfig.size()]));
        complexFractalParams.runParams.fromString(runConfig.toArray(new String[runConfig.size()]));
        if (thread_data != null) {
            complexFractalParams.threadDataFromString(thread_data);
        }
        if (post_process_mode != null) {
            complexFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        if (zooms != null) {
            complexFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));
        }
        if (switch_rate != null) {
            complexFractalParams.initParams.setSwitchRate(Integer.parseInt(switch_rate));
        }
        if (trap_point != null) {
            complexFractalParams.initParams.setTrapPoint(new Complex(trap_point));
        }
        if (trap_line != null) {
            complexFractalParams.initParams.setLineTrap(trap_line);
        }
        if (oldvariablecode != null) {
            complexFractalParams.initParams.setOldVariableCode(oldvariablecode);
        }
        complexFractalParams.setPath(paramfile.getAbsolutePath());
        return complexFractalParams;
    }
    @NotNull
    public static IFSFractalParams getIFSParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull ArrayList<String> lines = new ArrayList<>(prepareBatchLines(paramfile)), linesBk = new ArrayList<>(lines);
        @Nullable String frameskip = null, post_process_mode = null, threads = null;
        for (String line : lines) {
            threads = processCommand(linesBk, line, THREADS);
            post_process_mode = processCommand(linesBk, line, POSTPROCESSING);
            frameskip = processCommand(linesBk, line, FRAMESKIP);
        }
        lines = linesBk;
        @Nullable List<String> zooms = null;
        if (lines.indexOf(ZOOMS) >= 0) {
            zooms = lines.subList(lines.indexOf(ZOOMS) + 1, lines.indexOf(ENDZOOMS));
        }
        @NotNull IFSFractalParams ifsFractalParams = new IFSFractalParams();
        ifsFractalParams.fromString(lines.toArray(new String[lines.size()]));
        if (zooms != null) {
            ifsFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));
        }
        if (frameskip != null) {
            ifsFractalParams.setFrameskip(Integer.parseInt(frameskip));
        }
        if (threads != null) {
            ifsFractalParams.setThreads(Integer.parseInt(threads));
        }
        if (post_process_mode != null) {
            ifsFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        ifsFractalParams.setPath(paramfile.getAbsolutePath());
        return ifsFractalParams;
    }
    @NotNull
    public static ComplexBrotFractalParams getComplexBrotParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull ArrayList<String> lines = new ArrayList<>(prepareBatchLines(paramfile)), linesBk = new ArrayList<>(lines);
        @Nullable String constant = null, threads = null, post_process_mode = null, oldvariablecode = null, switch_rate = null, logscaling = null;
        for (String line : lines) {
            threads = processCommand(linesBk, line, THREADS);
            switch_rate = processCommand(linesBk, line, SWITCH_RATE);
            post_process_mode = processCommand(linesBk, line, POSTPROCESSING);
            constant = processCommand(linesBk, line, NEWTON_CONSTANT);
            oldvariablecode = processCommand(linesBk, line, OLD_VARIABLE_CODE);
            logscaling = processCommand(linesBk, line, USE_LOG_SCALING);
        }
        lines = linesBk;
        @Nullable List<String> zooms = null;
        if (lines.indexOf(ZOOMS) >= 0) {
            zooms = lines.subList(lines.indexOf(ZOOMS) + 1, lines.indexOf(ENDZOOMS));
        }
        @NotNull ComplexBrotFractalParams complexBrotFractalParams = new ComplexBrotFractalParams();
        complexBrotFractalParams.fromString(lines.toArray(new String[lines.size()]));
        if (zooms != null) {
            complexBrotFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));
        }
        if (constant != null) {
            complexBrotFractalParams.setNewton_constant(new Complex(constant));
        }
        if (threads != null) {
            complexBrotFractalParams.setNum_threads(Integer.parseInt(threads));
        }
        if (post_process_mode != null) {
            complexBrotFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        if (switch_rate != null) {
            complexBrotFractalParams.setSwitch_rate(Integer.parseInt(switch_rate));
        }
        if (oldvariablecode != null) {
            complexBrotFractalParams.setOldVariableCode(oldvariablecode);
        }
        if (logscaling != null) {
            complexBrotFractalParams.setLogScaling(Boolean.parseBoolean(logscaling));
        }
        complexBrotFractalParams.setPath(paramfile.getAbsolutePath());
        return complexBrotFractalParams;
    }
    @NotNull
    public static LSFractalParams getLSParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull ArrayList<String> lines = new ArrayList<>(prepareBatchLines(paramfile)), linesBk = new ArrayList<>(lines);
        @Nullable String post_process_mode = null;
        for (String line : lines) {
            post_process_mode = processCommand(linesBk, line, POSTPROCESSING);
        }
        lines = linesBk;
        @NotNull LSFractalParams lsFractalParams = new LSFractalParams();
        lsFractalParams.fromString(lines.toArray(new String[lines.size()]));
        if (post_process_mode != null) {
            lsFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        lsFractalParams.setPath(paramfile.getAbsolutePath());
        return lsFractalParams;
    }
    private static String processCommand(List<String> ins, String in, String command) {
        String result = null;
        if (in.startsWith(command)) {
            result = in.substring(command.length()).trim();
            ins.remove(in);
        }
        return result;
    }
}