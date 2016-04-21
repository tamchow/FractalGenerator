package in.tamchow.fractal.config;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalConfig;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalConfig;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.strings.StringManipulator;
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
    public static ImageConfig getImageConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(cfgfile);
        @Nullable String dimensions = null;
        @NotNull ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals(IMAGE)) {
            return null;
        }
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith(COMMENT)) {
                if (line.startsWith(DIMENSIONS)) {
                    dimensions = line.substring(DIMENSIONS.length()).trim();
                    continue;
                }
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        @NotNull String[] imgparams = new String[lines.size()];
        lines.toArray(imgparams);
        @NotNull ImageConfig imageConfig = new ImageConfig();
        imageConfig.fromString(imgparams);
        if (dimensions != null) {
            @NotNull String[] parts = StringManipulator.split(dimensions, ",");
            imageConfig.setWidth(Integer.valueOf(parts[0]));
            imageConfig.setHeight(Integer.valueOf(parts[1]));
        }
        return imageConfig;
    }
    public static ComplexFractalConfig getComplexFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(cfgfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals(COMPLEX)) {
            return null;
        }
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        @NotNull List<String> globalcfg = lines.subList(lines.indexOf(GLOBALS) + 1, lines.indexOf(ENDGLOBALS));
        @NotNull List<String> specCfg = lines.subList(lines.indexOf(FRACTALS) + 1, lines.indexOf(ENDFRACTALS));
        @NotNull ComplexFractalConfig complexFractalConfig = new ComplexFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        @NotNull ComplexFractalParams[] complexFractalParams = new ComplexFractalParams[specCfg.size()];
        for (int i = 0; i < complexFractalParams.length; i++) {
            complexFractalParams[i] = getComplexParamFromFile(new File(specCfg.get(i)));
        }
        complexFractalConfig.setParams(complexFractalParams);
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
        @Nullable String[] zooms = null;
        if (lines.indexOf(ZOOMS) >= 0) {
            @NotNull List<String> zoomsConfig = lines.subList(lines.indexOf(ZOOMS) + 1, lines.indexOf(ENDZOOMS));
            zooms = new String[zoomsConfig.size()];
            zoomsConfig.toArray(zooms);
        }
        @NotNull List<String> initConfig = lines.subList(lines.indexOf(INIT) + 1, lines.indexOf(ENDINIT));
        @NotNull String[] init = new String[initConfig.size()];
        initConfig.toArray(init);
        @NotNull List<String> runConfig = lines.subList(lines.indexOf(RUN) + 1, lines.indexOf(ENDRUN));
        @NotNull String[] run = new String[runConfig.size()];
        initConfig.toArray(run);
        @NotNull ComplexFractalParams complexFractalParams = new ComplexFractalParams();
        complexFractalParams.initParams.fromString(init);
        complexFractalParams.runParams.fromString(run);
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
    public static IFSFractalConfig getIFSFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(cfgfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals(IFS)) {
            return null;
        }
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        @NotNull List<String> globalcfg = lines.subList(lines.indexOf(GLOBALS) + 1, lines.indexOf(ENDGLOBALS));
        @NotNull List<String> specCfg = lines.subList(lines.indexOf(FRACTALS) + 1, lines.indexOf(ENDFRACTALS));
        @NotNull IFSFractalConfig ifsFractalConfig = new IFSFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        @NotNull IFSFractalParams[] ifsFractalParams = new IFSFractalParams[specCfg.size()];
        for (int i = 0; i < ifsFractalParams.length; i++) {
            ifsFractalParams[i] = getIFSParamFromFile(new File(specCfg.get(i)));
        }
        ifsFractalConfig.setParams(ifsFractalParams);
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
        @Nullable String[] zooms = null;
        if (lines.indexOf(ZOOMS) >= 0) {
            @NotNull List<String> zoomsConfig = lines.subList(lines.indexOf(ZOOMS) + 1, lines.indexOf(ENDZOOMS));
            zooms = new String[zoomsConfig.size()];
            zoomsConfig.toArray(zooms);
        }
        @NotNull String[] params = new String[lines.size()];
        lines.toArray(params);
        @NotNull IFSFractalParams ifsFractalParams = IFSFractalParams.fromString(params);
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
    public static ComplexBrotFractalConfig getComplexBrotFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(cfgfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals(COMPLEXBROT)) {
            return null;
        }
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        @NotNull List<String> globalcfg = lines.subList(lines.indexOf(GLOBALS) + 1, lines.indexOf(ENDGLOBALS));
        @NotNull List<String> specCfg = lines.subList(lines.indexOf(FRACTALS) + 1, lines.indexOf(ENDFRACTALS));
        @NotNull ComplexBrotFractalConfig complexBrotFractalConfig = new ComplexBrotFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        @NotNull ComplexBrotFractalParams[] complexBrotFractalParams = new ComplexBrotFractalParams[specCfg.size()];
        for (int i = 0; i < complexBrotFractalParams.length; i++) {
            complexBrotFractalParams[i] = getComplexBrotParamFromFile(new File(specCfg.get(i)));
        }
        complexBrotFractalConfig.setParams(complexBrotFractalParams);
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
        @Nullable String[] zooms = null;
        if (lines.indexOf(ZOOMS) >= 0) {
            @NotNull List<String> zoomsConfig = lines.subList(lines.indexOf(ZOOMS) + 1, lines.indexOf(ENDZOOMS));
            zooms = new String[zoomsConfig.size()];
            zoomsConfig.toArray(zooms);
        }
        @NotNull String[] params = new String[lines.size()];
        lines.toArray(params);
        @NotNull ComplexBrotFractalParams complexBrotFractalParams = new ComplexBrotFractalParams();
        complexBrotFractalParams.fromString(params);
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
    public static LSFractalConfig getLSFractalConfigFromFile(@NotNull File cfgfile) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(cfgfile);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals(LS)) {
            return null;
        }
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith(COMMENT)) {
                if (line.contains(COMMENT)) {
                    line = line.substring(0, line.indexOf(COMMENT)).trim();
                }
                lines.add(line);
            }
        }
        @NotNull List<String> globalcfg = lines.subList(lines.indexOf(GLOBALS) + 1, lines.indexOf(ENDGLOBALS));
        @NotNull List<String> specCfg = lines.subList(lines.indexOf(FRACTALS) + 1, lines.indexOf(ENDFRACTALS));
        @NotNull LSFractalConfig ifsFractalConfig = new LSFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        @NotNull LSFractalParams[] ifsFractalParams = new LSFractalParams[specCfg.size()];
        for (int i = 0; i < ifsFractalParams.length; i++) {
            ifsFractalParams[i] = getLSParamFromFile(new File(specCfg.get(i)));
        }
        ifsFractalConfig.setParams(ifsFractalParams);
        return ifsFractalConfig;
    }
    @NotNull
    private static LSFractalParams getLSParamFromFile(@NotNull File file) throws FileNotFoundException {
        @NotNull Scanner in = new Scanner(file);
        @NotNull ArrayList<String> lines = new ArrayList<>();
        @Nullable String post_process_mode = null, fps = null;
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
        @NotNull String[] params = new String[lines.size()];
        lines.toArray(params);
        @NotNull LSFractalParams lsFractalParams = new LSFractalParams();
        lsFractalParams.fromString(params);
        if (post_process_mode != null) {
            lsFractalParams.setPostProcessMode(PixelContainer.PostProcessMode.valueOf(post_process_mode));
        }
        lsFractalParams.setPath(file.getAbsolutePath());
        return lsFractalParams;
    }
}