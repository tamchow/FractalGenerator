package in.tamchow.fractal.config;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalConfig;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complexbrot.ComplexBrotFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.math.complex.Complex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 * Reads the configuration of fractals or images from a file
 */
public class ConfigReader {
    public static boolean isFileImageConfig(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals("[ImageConfig]");}
    public static boolean isFileComplexFractalConfig(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals("[ComplexFractalConfig]");}
    public static boolean isFileComplexBrotFractalConfig(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals("[ComplexBrotFractalConfig]");
    }
    public static boolean isFileIFSFractalConfig(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals("[IFSFractalConfig]");}
    public static ImageConfig getImageConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile); String dimensions = null;
        ArrayList<String> lines = new ArrayList<>(); if (!in.nextLine().equals("[ImageConfig]")) {return null;}
        while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.startsWith("Dimensions:")) {
                    dimensions = line.substring("Dimensions:".length()).trim(); continue;
                } if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } String[] imgparams = new String[lines.size()]; lines.toArray(imgparams);
        ImageConfig imageConfig = new ImageConfig(); imageConfig.fromString(imgparams); if (dimensions != null) {
            imageConfig.setWidth(Integer.valueOf(dimensions.split(",")[0]));
            imageConfig.setHeight(Integer.valueOf(dimensions.split(",")[1]));
        } return imageConfig;
    }
    public static ComplexFractalConfig getComplexFractalConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile); ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals("[ComplexFractalConfig]")) {return null;} while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } List<String> globalcfg = lines.subList(lines.indexOf("[Globals]") + 1, lines.indexOf("[EndGlobals]"));
        List<String> specCfg = lines.subList(lines.indexOf("[Fractals]") + 1, lines.indexOf("[EndFractals]"));
        ComplexFractalConfig complexFractalConfig = new ComplexFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        ComplexFractalParams[] complexFractalParams = new ComplexFractalParams[specCfg.size()];
        for (int i = 0; i < complexFractalParams.length; i++) {
            complexFractalParams[i] = getComplexParamFromFile(new File(specCfg.get(i)));
        } complexFractalConfig.setParams(complexFractalParams); return complexFractalConfig;
    }
    public static ComplexFractalParams getComplexParamFromFile(File paramfile) throws FileNotFoundException {
        Scanner in = new Scanner(paramfile); ArrayList<String> lines = new ArrayList<>();
        String thread_data = null, post_process_mode = null, switch_rate = null, trap_point = null, trap_line = null, oldvariablecode = null;
        while (in.hasNext()) {
            String line = in.nextLine(); if (line.startsWith("Threads:")) {
                thread_data = line.substring("Threads:".length()).trim(); continue;}
            if (line.startsWith("Switch_Mode_Rate:")) {
                switch_rate = line.substring("Switch_Mode_Rate:".length()).trim(); continue;
            } if (line.startsWith("Postprocessing:")) {
                post_process_mode = line.substring("Postprocessing:".length()).trim(); continue;
            } if (line.startsWith("Trap_point:")) {
                trap_point = line.substring("Trap_point:".length()).trim(); continue;
            } if (line.startsWith("Trap_line:")) {
                trap_line = line.substring("Trap_line:".length()).trim(); continue;
            } if (line.startsWith("Old_variable_code:")) {
                oldvariablecode = line.substring("Old_variable_code:".length()).trim(); continue;
            } if (!line.startsWith("#")) {
                if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } String[] zooms = null; if (lines.indexOf("[Zooms]") >= 0) {
            List<String> zoomsConfig = lines.subList(lines.indexOf("[Zooms]") + 1, lines.indexOf("[EndZooms]"));
            zooms = new String[zoomsConfig.size()]; zoomsConfig.toArray(zooms);}
        List<String> initConfig = lines.subList(lines.indexOf("[Initconfig]") + 1, lines.indexOf("[EndInitconfig]"));
        String[] init = new String[initConfig.size()]; initConfig.toArray(init);
        List<String> runConfig = lines.subList(lines.indexOf("[Runconfig]") + 1, lines.indexOf("[EndRunconfig]"));
        String[] run = new String[runConfig.size()];
        initConfig.toArray(run); ComplexFractalParams complexFractalParams = new ComplexFractalParams();
        complexFractalParams.initParams.fromString(init); complexFractalParams.runParams.fromString(run);
        if (thread_data != null) {complexFractalParams.threadDataFromString(thread_data);}
        if (post_process_mode != null) {
            complexFractalParams.setPostprocessMode(ImageData.PostProcessMode.valueOf(post_process_mode));
        }
        if (zooms != null) {complexFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));}
        if (switch_rate != null) {complexFractalParams.initParams.setSwitch_rate(Integer.valueOf(switch_rate));}
        if (trap_point != null) {complexFractalParams.initParams.setTrap_point(new Complex(trap_point));}
        if (trap_line != null) {complexFractalParams.initParams.setLinetrap(trap_line);}
        if (oldvariablecode != null) {complexFractalParams.initParams.setOldvariablecode(oldvariablecode);}
        complexFractalParams.setPath(paramfile.getAbsolutePath());
        return complexFractalParams;
    }
    public static IFSFractalConfig getIFSFractalConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile); ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals("[IFSFractalConfig]")) {return null;} while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } List<String> globalcfg = lines.subList(lines.indexOf("[Globals]") + 1, lines.indexOf("[EndGlobals]"));
        List<String> specCfg = lines.subList(lines.indexOf("[Fractals]") + 1, lines.indexOf("[EndFractals]"));
        IFSFractalConfig ifsFractalConfig = new IFSFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        IFSFractalParams[] ifsFractalParams = new IFSFractalParams[specCfg.size()];
        for (int i = 0; i < ifsFractalParams.length; i++) {
            ifsFractalParams[i] = getIFSParamFromFile(new File(specCfg.get(i)));
        } ifsFractalConfig.setParams(ifsFractalParams); return ifsFractalConfig;}
    public static IFSFractalParams getIFSParamFromFile(File paramfile) throws FileNotFoundException {
        Scanner in = new Scanner(paramfile); ArrayList<String> lines = new ArrayList<>();
        String frameskip = null, post_process_mode = null;
        while (in.hasNext()) {
            String line = in.nextLine(); if (line.startsWith("Frameskip:")) {
                frameskip = line.substring("Frameskip:".length()).trim();
            } if (line.startsWith("Postprocessing:")) {
                post_process_mode = line.substring("Postprocessing:".length()).trim(); continue;
            } if (!line.startsWith("#")) {
                if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } String[] zooms = null; if (lines.indexOf("[Zooms]") >= 0) {
            List<String> zoomsConfig = lines.subList(lines.indexOf("[Zooms]") + 1, lines.indexOf("[EndZooms]"));
            zooms = new String[zoomsConfig.size()]; zoomsConfig.toArray(zooms);
        } String[] params = new String[lines.size()]; lines.toArray(params);
        IFSFractalParams ifsFractalParams = IFSFractalParams.fromString(params);
        if (zooms != null) {ifsFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));}
        if (frameskip != null) {ifsFractalParams.setFrameskip(Integer.valueOf(frameskip));}
        if (post_process_mode != null) {
            ifsFractalParams.setPostprocessMode(ImageData.PostProcessMode.valueOf(post_process_mode));
        }
        ifsFractalParams.setPath(paramfile.getAbsolutePath()); return ifsFractalParams;
    }
    public static ComplexBrotFractalConfig getComplexBrotFractalConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile); ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals("[ComplexBrotFractalConfig]")) {return null;} while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } List<String> globalcfg = lines.subList(lines.indexOf("[Globals]") + 1, lines.indexOf("[EndGlobals]"));
        List<String> specCfg = lines.subList(lines.indexOf("[Fractals]") + 1, lines.indexOf("[EndFractals]"));
        ComplexBrotFractalConfig complexBrotFractalConfig = new ComplexBrotFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        ComplexBrotFractalParams[] complexBrotFractalParams = new ComplexBrotFractalParams[specCfg.size()];
        for (int i = 0; i < complexBrotFractalParams.length; i++) {
            complexBrotFractalParams[i] = getComplexBrotParamFromFile(new File(specCfg.get(i)));
        } complexBrotFractalConfig.setParams(complexBrotFractalParams); return complexBrotFractalConfig;
    }
    private static ComplexBrotFractalParams getComplexBrotParamFromFile(File paramfile) throws FileNotFoundException {
        Scanner in = new Scanner(paramfile); ArrayList<String> lines = new ArrayList<>();
        String post_process_mode = null, constant = null; while (in.hasNext()) {
            String line = in.nextLine(); if (line.startsWith("Postprocessing:")) {
                post_process_mode = line.substring("Postprocessing:".length()).trim(); continue;
            } if (line.startsWith("Newton_constant:")) {
                post_process_mode = line.substring("Newton_constant:".length()).trim(); continue;
            } if (!line.startsWith("#")) {
                if (line.contains("#")) {line = line.substring(0, line.indexOf("#")).trim();} lines.add(line);
            }
        } String[] zooms = null; if (lines.indexOf("[Zooms]") >= 0) {
            List<String> zoomsConfig = lines.subList(lines.indexOf("[Zooms]") + 1, lines.indexOf("[EndZooms]"));
            zooms = new String[zoomsConfig.size()]; zoomsConfig.toArray(zooms);
        } String[] params = new String[lines.size()]; lines.toArray(params);
        ComplexBrotFractalParams complexBrotFractalParams = new ComplexBrotFractalParams();
        complexBrotFractalParams.fromString(params);
        if (zooms != null) {complexBrotFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));}
        if (constant != null) {complexBrotFractalParams.setNewton_constant(new Complex(constant));}
        if (post_process_mode != null) {
            complexBrotFractalParams.setPostprocessMode(ImageData.PostProcessMode.valueOf(post_process_mode));
        } complexBrotFractalParams.setPath(paramfile.getAbsolutePath()); return complexBrotFractalParams;
    }
}