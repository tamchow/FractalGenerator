package in.tamchow.fractal.platform_tools;

import in.tamchow.fractal.imgutils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reads the configuration of fractals or images from a file
 */
public class ConfigReader {
    public static ImageConfig getImageConfigFromFile(File cfgfile)throws FileNotFoundException{
        Scanner in=new Scanner(cfgfile);
        ArrayList<String> lines=new ArrayList<>();
        while (in.hasNext()){
            String line=in.nextLine();
            if(!line.startsWith("#")){
                lines.add(line);
            }
        }
        List<String> globalcfg=lines.subList(lines.indexOf("[Globals]")+1,lines.indexOf("[EndGlobals]"));
        List<String> specCfg=lines.subList(lines.indexOf("[Images]")+1,lines.indexOf("[EndImages]"));
        ImageConfig imageConfig=new ImageConfig(Integer.valueOf(globalcfg.get(0)),Integer.valueOf(globalcfg.get(1)),Integer.valueOf(globalcfg.get(2)));
        ImageParams[] imgparams=new ImageParams[specCfg.size()];
        for (int i=0;i<specCfg.size();i++){
            imgparams[i]=new ImageParams(specCfg.get(i).substring(0,specCfg.get(i).indexOf(" ")),Integer.valueOf(specCfg.get(i).substring(specCfg.get(i).indexOf(" ")+1,specCfg.get(i).length())));
        }
        imageConfig.readConfig(imgparams);
        return imageConfig;
    }
    public static FractalConfig getFractalConfigFromFile(File cfgfile)throws FileNotFoundException{
        Scanner in=new Scanner(cfgfile);
        ArrayList<String> lines=new ArrayList<>();
        while (in.hasNext()){
            String line=in.nextLine();
            if(!line.startsWith("#")){
                lines.add(line);
            }
        }
        FractalConfig fractalConfig=new FractalConfig(Integer.valueOf(lines.get(0)),Integer.valueOf(lines.get(1)),Integer.valueOf(lines.get(2)));
        lines.remove(0);lines.remove(1);lines.remove(2);
        FractalParams[] fractalParams=new FractalParams[lines.size()];
        for (int i=0;i<fractalParams.length;i++){
            fractalParams[i]=getParamFromFile(new File(lines.get(i)));
        }
        fractalConfig.setParams(fractalParams);
        return fractalConfig;
    }
    private static FractalParams getParamFromFile(File paramfile)throws FileNotFoundException{
        Scanner in=new Scanner(paramfile);
        ArrayList<String> lines=new ArrayList<>();
        while (in.hasNext()){
            String line=in.nextLine();
            if(!line.startsWith("#")){
                lines.add(line);
            }
        }
        List<String> initConfig=lines.subList(lines.indexOf("[Initconfig]")+1,lines.indexOf("[EndInitconfig]"));
        String[] init=new String[initConfig.size()];
        initConfig.toArray(init);
        List<String> runConfig=lines.subList(lines.indexOf("[Runconfig]")+1,lines.indexOf("EndRunconfig]"));
        String[] run=new String[runConfig.size()];
        initConfig.toArray(run);
        FractalParams fractalParams=new FractalParams();
        fractalParams.initParams.paramsFromString(init);
        fractalParams.runParams.paramsFromString(run);
        return fractalParams;
    }
}
