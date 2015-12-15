package in.tamchow.fractal.config;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalConfig;
import in.tamchow.fractal.config.fractalconfig.IFS.IFSFractalParams;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
import in.tamchow.fractal.config.fractalconfig.fractal_zooms.ZoomConfig;
import in.tamchow.fractal.config.imageconfig.ImageConfig;
import in.tamchow.fractal.config.imageconfig.ImageParams;

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
    public static boolean isFileIFSFractalConfig(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine().equals("[IFSFractalConfig]");}
    public static ImageConfig getImageConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile);
        ArrayList<String> lines = new ArrayList<>(); if (!in.nextLine().equals("[ImageConfig]")) {return null;}
        while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                } lines.add(line);}
        } List<String> globalcfg = lines.subList(lines.indexOf("[Globals]") + 1, lines.indexOf("[EndGlobals]"));
        List<String> specCfg = lines.subList(lines.indexOf("[Images]") + 1, lines.indexOf("[EndImages]"));
        ImageConfig imageConfig = new ImageConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        ImageParams[] imgparams = new ImageParams[specCfg.size()];
        for (int i = 0; i < specCfg.size(); i++) {
            imgparams[i] = new ImageParams(specCfg.get(i).substring(0, specCfg.get(i).indexOf(" ")), Integer.valueOf(specCfg.get(i).substring(specCfg.get(i).indexOf(" ") + 1, specCfg.get(i).length())));
        }
        imageConfig.readConfig(imgparams);
        return imageConfig;
    }
    public static ComplexFractalConfig getComplexFractalConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile); ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals("[ComplexFractalConfig]")) {return null;}
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                }
                lines.add(line);
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
        Scanner in = new Scanner(paramfile);
        ArrayList<String> lines = new ArrayList<>(); String thread_data=null;
        while (in.hasNext()) {
            String line = in.nextLine(); if (line.startsWith("Threads:")) {
                thread_data = line.substring("Threads:".length()).trim(); continue;}
            if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                } lines.add(line);
            }
        } String[] zooms = null; if (lines.indexOf("[Zooms]") >= 0) {
            List<String> zoomsConfig = lines.subList(lines.indexOf("[Zooms]") + 1, lines.indexOf("[EndZooms]"));
            zooms = new String[zoomsConfig.size()]; zoomsConfig.toArray(zooms);}
        List<String> initConfig = lines.subList(lines.indexOf("[Initconfig]") + 1, lines.indexOf("[EndInitconfig]"));
        String[] init = new String[initConfig.size()];
        initConfig.toArray(init);
        List<String> runConfig = lines.subList(lines.indexOf("[Runconfig]") + 1, lines.indexOf("[EndRunconfig]"));
        String[] run = new String[runConfig.size()];
        initConfig.toArray(run); ComplexFractalParams complexFractalParams = new ComplexFractalParams();
        complexFractalParams.initParams.fromString(init); complexFractalParams.runParams.fromString(run);
        if (thread_data != null) {complexFractalParams.threadDataFromString(thread_data);}
        if (zooms != null) {complexFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));}
        return complexFractalParams;
    }
    public static IFSFractalConfig getIFSFractalConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile); ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals("[IFSFractalConfig]")) {return null;} while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                } lines.add(line);
            }
        } List<String> globalcfg = lines.subList(lines.indexOf("[Globals]") + 1, lines.indexOf("[EndGlobals]"));
        List<String> specCfg = lines.subList(lines.indexOf("[Fractals]") + 1, lines.indexOf("[EndFractals]"));
        IFSFractalConfig ifsFractalConfig = new IFSFractalConfig(Integer.valueOf(globalcfg.get(0)), Integer.valueOf(globalcfg.get(1)), Integer.valueOf(globalcfg.get(2)));
        IFSFractalParams[] ifsFractalParams = new IFSFractalParams[specCfg.size()];
        for (int i = 0; i < ifsFractalParams.length; i++) {
            ifsFractalParams[i] = getIFSParamFromFile(new File(specCfg.get(i)));
        } ifsFractalConfig.setParams(ifsFractalParams); return ifsFractalConfig;}
    public static IFSFractalParams getIFSParamFromFile(File paramfile) throws FileNotFoundException {
        Scanner in = new Scanner(paramfile); ArrayList<String> lines = new ArrayList<>(); while (in.hasNext()) {
            String line = in.nextLine(); if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                } lines.add(line);
            }
        } String[] zooms = null; if (lines.indexOf("[Zooms]") >= 0) {
            List<String> zoomsConfig = lines.subList(lines.indexOf("[Zooms]") + 1, lines.indexOf("[EndZooms]"));
            zooms = new String[zoomsConfig.size()]; zoomsConfig.toArray(zooms);
        } String[] params = new String[lines.size()]; lines.toArray(params);
        IFSFractalParams ifsFractalParams = IFSFractalParams.fromString(params);
        if (zooms != null) {ifsFractalParams.setZoomConfig(ZoomConfig.fromString(zooms));} return ifsFractalParams;
    }
}