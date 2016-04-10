package in.tamchow.fractal.config.imageconfig;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.transition.TransitionTypes;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
/**
 * Parameters for configuring an image
 */
public class ImageParams extends Config implements Serializable, DataFromString {
    public PixelContainer image;
    public TransitionTypes transition;
    public ImageParams() {
    }
    public ImageParams(@NotNull ImageParams old) {
        initParams(old.transtime, old.fps, old.wait, old.image, old.transition);
    }
    public ImageParams(int transtime, int fps, int wait, PixelContainer image, TransitionTypes transition) {
        initParams(transtime, fps, wait, image, transition);
    }
    public ImageParams(int transtime, int fps, int wait, String path, TransitionTypes transition) {
        initParams(transtime, fps, wait, path, transition);
    }
    public ImageParams(int transtime, int fps, int wait, PixelContainer image) {
        initParams(transtime, fps, wait, image, TransitionTypes.NONE);
    }
    public ImageParams(int transtime, int fps, int wait, String path) {
        initParams(transtime, fps, wait, path, TransitionTypes.NONE);
    }
    private void initParams(int transtime, int fps, int wait, PixelContainer image, TransitionTypes transition) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        this.image = new PixelContainer(image);
        this.transition = transition;
    }
    private void initParams(int transtime, int fps, int wait, String path, TransitionTypes transition) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        this.image = new PixelContainer(path);
        this.transition = transition;
    }
    @NotNull
    public String toString() {
        return transtime + "," + fps + "," + wait + "," + image.getPath() + "," + transition;
    }
    public void fromString(@NotNull String params) {
        fromString(params.split(","));
    }
    public void fromString(String[] params) {
        initParams(Integer.valueOf(params[0]), Integer.valueOf(params[1]), Integer.valueOf(params[2]), params[3], TransitionTypes.valueOf(params[4]));
    }
}