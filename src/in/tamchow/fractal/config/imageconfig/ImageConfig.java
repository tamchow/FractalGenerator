package in.tamchow.fractal.config.imageconfig;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.imgutils.ImageData;

import java.util.Arrays;
/**
 * Stores configuration data for the image display function
 */
public class ImageConfig extends Config {
    ImageData[] images;
    int[] transitions;
    public ImageConfig(int transtime, int fps, ImageData[] data, int[] transitions, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setImages(data);
        setTransitions(transitions);
        setWait(wait);
    }
    public ImageConfig(int transtime, int fps, int wait) {
        setFps(fps);
        setTranstime(transtime);
        setWait(wait);
    }
    public ImageConfig(int fps, ImageData[] data, int wait) {
        setFps(fps);
        setTranstime(data.length / fps);
        setImages(data);
        transitions = new int[data.length];
        Arrays.fill(transitions, -1);
        setWait(wait);
    }
    public ImageData[] getImages() {
        return images;
    }
    public void setImages(ImageData[] images) {
        this.images = new ImageData[images.length];
        for (int i = 0; i < images.length; i++) {
            this.images[i] = new ImageData(images[i]);
        }
    }
    public int[] getTransitions() {
        return transitions;
    }
    public void setTransitions(int[] transitions) {
        this.transitions = new int[transitions.length];
        System.arraycopy(transitions, 0, this.transitions, 0, transitions.length);
    }
    public void readConfig(ImageParams[] config) {
        images = new ImageData[config.length];
        transitions = new int[config.length];
        for (int i = 0; i < config.length; i++) {
            images[i] = config[i].image;
            transitions[i] = config[i].transition;
        }
    }
}
