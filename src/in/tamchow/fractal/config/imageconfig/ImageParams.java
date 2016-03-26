package in.tamchow.fractal.config.imageconfig;

import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.TransitionTypes;

import java.io.Serializable;

/**
 * Parameters for configuring an image
 */
public class ImageParams extends Config implements Serializable, DataFromString {
    public ImageData image;
    public TransitionTypes transition;

    public ImageParams() {
    }

    public ImageParams(ImageParams old) {
        initParams(old.transtime, old.fps, old.wait, old.image, old.transition);
    }

    public ImageParams(int transtime, int fps, int wait, ImageData image, TransitionTypes transition) {
        initParams(transtime, fps, wait, image, transition);
    }

    public ImageParams(int transtime, int fps, int wait, String path, TransitionTypes transition) {
        initParams(transtime, fps, wait, path, transition);
    }

    public ImageParams(int transtime, int fps, int wait, ImageData image) {
        initParams(transtime, fps, wait, image, TransitionTypes.NONE);
    }

    public ImageParams(int transtime, int fps, int wait, String path) {
        initParams(transtime, fps, wait, path, TransitionTypes.NONE);
    }

    private void initParams(int transtime, int fps, int wait, ImageData image, TransitionTypes transition) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        this.image = new ImageData(image);
        this.transition = transition;
    }

    private void initParams(int transtime, int fps, int wait, String path, TransitionTypes transition) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        this.image = new ImageData(path);
        this.transition = transition;
    }

    public String toString() {
        return transtime + "," + fps + "," + wait + "," + image.getPath() + "," + transition;
    }

    public void fromString(String params) {
        fromString(params.split(","));
    }

    public void fromString(String[] params) {
        initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Integer.valueOf(params[2]), params[3], TransitionTypes.valueOf(params[4]));
    }
}