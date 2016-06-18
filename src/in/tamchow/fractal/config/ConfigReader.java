package in.tamchow.fractal.config;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.imageconfig.ImageParams;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
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
    public static BatchContainer<ComplexFractalParams> getComplexFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        List<String> lines = prepareBatchLines(cfgfile);
        if (!lines.get(0).equals(COMPLEX)) {
            return null;
        }
        lines.remove(0);
        @NotNull BatchContainer<ComplexFractalParams> complexFractalConfig = new BatchContainer<>();
        for (String line : lines) {
            complexFractalConfig.addItem(getComplexParamFromFile(new File(line)));
        }
        return complexFractalConfig;
    }
    @NotNull
    private static ComplexFractalParams getComplexParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(paramfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        @Nullable String thread_data = null, post_process_mode = null, switch_rate = null, trap_point = null, trap_line = null, oldvariablecode = null;
        while (in.hasNext()) {
            String line = in.nextLine();
            if (line.startsWith(THREADS)) {
                thread_data = line.substring(THREADS.length()).trim();
                continue;
            }
            if (line.startsWith(SWITCH_RATE)) {
                switch_rate = line.substring(SWITCH_RATE.length()).trim();
                continue;
            }
            if (line.startsWith(POSTPROCESSING)) {
                post_process_mode = line.substring(POSTPROCESSING.length()).trim();
                continue;
            }
            if (line.startsWith(TRAP_POINT)) {
                trap_point = line.substring(TRAP_POINT.length()).trim();
                continue;
            }
            if (line.startsWith(TRAP_LINE)) {
                trap_line = line.substring(TRAP_LINE.length()).trim();
                continue;
            }
            if (line.startsWith(OLD_VARIABLE_CODE)) {
                oldvariablecode = line.substring(OLD_VARIABLE_CODE.length()).trim();
                continue;
            }
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
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
            complexFractalParams.initParams.setSwitch_rate(Integer.valueOf(switch_rate));
        }
        if (trap_point != null) {
            complexFractalParams.initParams.setTrap_point(new Complex(trap_point));
        }
        if (trap_line != null) {
            complexFractalParams.initParams.setLinetrap(trap_line);
        }
        if (oldvariablecode != null) {
            complexFractalParams.initParams.setOldvariablecode(oldvariablecode);
        }
        complexFractalParams.setPath(paramfile.getAbsolutePath());
        return complexFractalParams;
    }
    public static BatchContainer<IFSFractalParams> getIFSFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        List<String> lines = prepareBatchLines(cfgfile);
        if (!lines.get(0).equals(IFS)) {
            return null;
        }
        lines.remove(0);
        @NotNull BatchContainer<IFSFractalParams> ifsFractalConfig = new BatchContainer<>();
        for (String line : lines) {
            ifsFractalConfig.addItem(getIFSParamFromFile(new File(line)));
        }
        return ifsFractalConfig;
    }
    @NotNull
    private static IFSFractalParams getIFSParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(paramfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        @Nullable String frameskip = null, post_process_mode = null, threads = null;
        while (in.hasNext()) {
            String line = in.nextLine();
            if (line.startsWith(FRAMESKIP)) {
                frameskip = line.substring(FRAMESKIP.length()).trim();
            }
            if (line.startsWith(THREADS)) {
                threads = line.substring(THREADS.length()).trim();
            }
            if (line.startsWith(POSTPROCESSING)) {
                post_process_mode = line.substring(POSTPROCESSING.length()).trim();
                continue;
            }
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
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
            ifsFractalParams.setFrameskip(Integer.valueOf(frameskip));
        }
        if (threads != null) {
            ifsFractalParams.setThreads(Integer.valueOf(threads));
        }
        if (post_process_mode != null) {
            ifsFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        ifsFractalParams.setPath(paramfile.getAbsolutePath());
        return ifsFractalParams;
    }
    public static BatchContainer<ComplexBrotFractalParams> getComplexBrotFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        List<String> lines = prepareBatchLines(cfgfile);
        if (!lines.get(0).equals(COMPLEXBROT)) {
            return null;
        }
        lines.remove(0);
        @NotNull BatchContainer<ComplexBrotFractalParams> complexBrotFractalConfig = new BatchContainer<>();
        for (String line : lines) {
            complexBrotFractalConfig.addItem(getComplexBrotParamFromFile(new File(line)));
        }
        return complexBrotFractalConfig;
    }
    @NotNull
    private static ComplexBrotFractalParams getComplexBrotParamFromFile(@NotNull File paramfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(paramfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        @Nullable String constant = null, threads = null, post_process_mode = null, oldvariablecode = null, switch_rate = null;
        while (in.hasNext()) {
            String line = in.nextLine();
            if (line.startsWith(THREADS)) {
                threads = line.substring(THREADS.length()).trim();
            }
            if (line.startsWith(POSTPROCESSING)) {
                post_process_mode = line.substring(POSTPROCESSING.length()).trim();
                continue;
            }
            if (line.startsWith(NEWTON_CONSTANT)) {
                constant = line.substring(NEWTON_CONSTANT.length()).trim();
                continue;
            }
            if (line.startsWith(OLD_VARIABLE_CODE)) {
                oldvariablecode = line.substring(OLD_VARIABLE_CODE.length()).trim();
                continue;
            }
            if (line.startsWith(SWITCH_RATE)) {
                switch_rate = line.substring(SWITCH_RATE.length()).trim();
                continue;
            }
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
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
            complexBrotFractalParams.setNum_threads(Integer.valueOf(threads));
        }
        if (post_process_mode != null) {
            complexBrotFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        if (switch_rate != null) {
            complexBrotFractalParams.setSwitch_rate(Integer.valueOf(switch_rate));
        }
        if (oldvariablecode != null) {
            complexBrotFractalParams.setOldVariableCode(oldvariablecode);
        }
        complexBrotFractalParams.setPath(paramfile.getAbsolutePath());
        return complexBrotFractalParams;
    }
    public static BatchContainer<LSFractalParams> getLSFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        List<String> lines = prepareBatchLines(cfgfile);
        if (!lines.get(0).equals(LS)) {
            return null;
        }
        lines.remove(0);
        @NotNull BatchContainer<LSFractalParams> lsFractalConfig = new BatchContainer<>();
        for (String line : lines) {
            lsFractalConfig.addItem(getLSParamFromFile(new File(line)));
        }
        return lsFractalConfig;
    }
    @NotNull
    private static LSFractalParams getLSParamFromFile(@NotNull File file) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(file);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        @Nullable String post_process_mode = null;
        while (in.hasNext()) {
            String line = in.nextLine();
            if (line.startsWith(POSTPROCESSING)) {
                post_process_mode = line.substring(POSTPROCESSING.length()).trim();
                continue;
            }
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        @NotNull LSFractalParams lsFractalParams = new LSFractalParams();
        lsFractalParams.fromString(lines.toArray(new String[lines.size()]));
        if (post_process_mode != null) {
            lsFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        lsFractalParams.setPath(file.getAbsolutePath());
        return lsFractalParams;
    }
}