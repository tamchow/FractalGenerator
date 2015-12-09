package in.tamchow.fractal.config;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalConfig;
import in.tamchow.fractal.config.fractalconfig.complex.ComplexFractalParams;
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
        Scanner in = new Scanner(file); return in.nextLine().equals("[ImageConfig]");
    }
    public static boolean isFileFractalConfig(File file) throws FileNotFoundException {
        Scanner in = new Scanner(file); return in.nextLine().equals("[ComplexFractalConfig]");
    }
    public static ImageConfig getImageConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile);
        ArrayList<String> lines = new ArrayList<>();
        if (!in.nextLine().equals("[ImageConfig]")) {
            return null;
        }
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                }
                lines.add(line);
            }
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
    public static ComplexFractalConfig getFractalConfigFromFile(File cfgfile) throws FileNotFoundException {
        Scanner in = new Scanner(cfgfile);
        ArrayList<String> lines = new ArrayList<>(); if (!in.nextLine().equals("[ComplexFractalConfig]")) {
            return null;
        }
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
            complexFractalParams[i] = getParamFromFile(new File(specCfg.get(i)));
        } complexFractalConfig.setParams(complexFractalParams); return complexFractalConfig;
    }
    public static ComplexFractalParams getParamFromFile(File paramfile) throws FileNotFoundException {
        Scanner in = new Scanner(paramfile);
        ArrayList<String> lines = new ArrayList<>();
        while (in.hasNext()) {
            String line = in.nextLine();
            if (!line.startsWith("#")) {
                if (line.contains("#")) {
                    line = line.substring(0, line.indexOf("#")).trim();
                }
                lines.add(line);
            }
        }
        List<String> initConfig = lines.subList(lines.indexOf("[Initconfig]") + 1, lines.indexOf("[EndInitconfig]"));
        String[] init = new String[initConfig.size()];
        initConfig.toArray(init);
        List<String> runConfig = lines.subList(lines.indexOf("[Runconfig]") + 1, lines.indexOf("[EndRunconfig]"));
        String[] run = new String[runConfig.size()];
        initConfig.toArray(run); ComplexFractalParams complexFractalParams = new ComplexFractalParams();
        complexFractalParams.initParams.paramsFromString(init); complexFractalParams.runParams.paramsFromString(run);
        return complexFractalParams;
    }
}
