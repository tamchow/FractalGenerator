package in.tamchow.fractal.config.imageconfig;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.DataFromString;
import in.tamchow.fractal.imgutils.ImageData;
import in.tamchow.fractal.imgutils.TransitionTypes;

import java.io.Serializable;
/**
 * Stores configuration data for the image display function
 */
public class ImageConfig extends Config implements DataFromString, Serializable {
    ImageParams[] params;
    int width, height;
    public ImageConfig() {}
    public ImageConfig(int transtime, int fps, ImageData[] data, TransitionTypes[] transitions, int wait) {
        setWidth(-1); setHeight(-1); setFps(fps); setTranstime(transtime); setWait(wait);
        params = new ImageParams[data.length]; for (int i = 0; i < params.length; i++) {
            params[i] = new ImageParams(transtime, fps, wait, data[i], transitions[i]);
        }
    }
    public ImageConfig(int transtime, int fps, int wait) {
        setWidth(-1); setHeight(-1); setFps(fps); setTranstime(transtime); setWait(wait); params = null;
    }
    public ImageConfig(int fps, ImageData[] data, int wait) {
        setWidth(-1); setHeight(-1); setFps(fps); setWait(wait); setTranstime(data.length / fps);
        params = new ImageParams[data.length]; for (int i = 0; i < params.length; i++) {
            params[i] = new ImageParams(transtime, fps, wait, data[i], TransitionTypes.NONE);
        }
    }
    public int getWidth() {return width;}
    public void setWidth(int width) {this.width = width;}
    public int getHeight() {return height;}
    public void setHeight(int height) {this.height = height;}
    public ImageParams[] getParams() {return params;}
    public void setParams(ImageParams[] params) {
        this.params = new ImageParams[params.length]; for (int i = 0; i < this.params.length; i++) {
            this.params[i] = new ImageParams(params[i]);
        }
    }
    public String toString() {
        String representation = "";
        if (customDimensions()) {representation += "Dimensions:" + width + "," + height + "\n";}
        for (ImageParams param : params) {representation += param + "\n";} return representation;
    }
    public boolean customDimensions() {return height >= 0 && width >= 0;}
    public void fromString(String[] params) {
        this.params = new ImageParams[params.length]; for (int i = 0; i < params.length; i++) {
            this.params[i] = new ImageParams(); this.params[i].fromString(params[i]);
        }
    }
}