package in.tamchow.fractal.config.imageconfig;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.imgutils.ImageData;

import java.io.Serializable;
/**
 * Parameters for configuring an image
 */
public class ImageParams extends Config implements Serializable, DataFromString {
    public ImageData image;
    public int transition;
    public ImageParams() {}
    public ImageParams(ImageParams old) {initParams(old.transtime, old.fps, old.wait, old.image, old.transition);}
    private void initParams(int transtime, int fps, int wait, ImageData image, int transition) {
        setFps(fps); setTranstime(transtime); setWait(wait);
        this.image = new ImageData(image);
        this.transition = transition;
    }
    public ImageParams(int transtime, int fps, int wait, ImageData image, int transition) {
        initParams(transtime, fps, wait, image, transition);
    }
    public ImageParams(int transtime, int fps, int wait, String path, int transition) {
        initParams(transtime, fps, wait, path, transition);
    }
    private void initParams(int transtime, int fps, int wait, String path, int transition) {
        setFps(fps); setTranstime(transtime); setWait(wait);
        this.image = new ImageData(path);
        this.transition = transition;
    }
    public ImageParams(int transtime, int fps, int wait, ImageData image) {
        initParams(transtime, fps, wait, image, -1);
    }
    public ImageParams(int transtime, int fps, int wait, String path) {
        initParams(transtime, fps, wait, path, -1);
    }
    public void fromString(String params) {fromString(params.split(","));}
    public void fromString(String[] params) {
        initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Integer.valueOf(params[2]), params[3], Integer.valueOf(params[4]));
    }
}