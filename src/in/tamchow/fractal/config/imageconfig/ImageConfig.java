package in.tamchow.fractal.config.imageconfig;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.transition.TransitionTypes;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;

import java.io.Serializable;
/**
 * Stores configuration data for the image display function
 */
public class ImageConfig extends Config implements DataFromString, Serializable {
    @Nullable
    ImageParams[] params;
    int width, height;
    public ImageConfig() {
    }
    public ImageConfig(int transtime, int fps, @NotNull PixelContainer[] data, TransitionTypes[] transitions, int wait) {
        setWidth(-1);
        setHeight(-1);
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        params = new ImageParams[data.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = new ImageParams(transtime, fps, wait, data[i], transitions[i]);
        }
    }
    public ImageConfig(int transtime, int fps, int wait) {
        setWidth(-1);
        setHeight(-1);
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
        params = null;
    }
    public ImageConfig(int fps, @NotNull PixelContainer[] data, int wait) {
        setWidth(-1);
        setHeight(-1);
        setFps(fps);
        setWait(wait);
        setTranstime(data.length / fps);
        params = new ImageParams[data.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = new ImageParams(transtime, fps, wait, data[i], TransitionTypes.NONE);
        }
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    @Nullable
    public ImageParams[] getParams() {
        return params;
    }
    public void setParams(@NotNull ImageParams[] params) {
        this.params = new ImageParams[params.length];
        for (int i = 0; i < this.params.length; i++) {
            this.params[i] = new ImageParams(params[i]);
        }
    }
    @NotNull
    public String toString() {
        @NotNull String representation = "[ImageConfig]\n";
        if (customDimensions()) {
            representation += "Dimensions:" + width + "," + height + "\n";
        }
        for (ImageParams param : params) {
            representation += param + "\n";
        }
        return representation;
    }
    public boolean customDimensions() {
        return height >= 0 && width >= 0;
    }
    public void fromString(@NotNull String[] params) {
        this.params = new ImageParams[params.length];
        for (int i = 0; i < params.length; i++) {
            this.params[i] = new ImageParams();
            this.params[i].fromString(params[i]);
        }
    }
}