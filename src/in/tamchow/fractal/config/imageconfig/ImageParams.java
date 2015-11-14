package in.tamchow.fractal.config.imageconfig;

import in.tamchow.fractal.imgutils.ImageData;

import java.io.Serializable;

/**
 * Parameters for configuring an image
 */
public class ImageParams implements Serializable {
    public ImageData image;
    public int transition;

    public ImageParams(ImageData image, int transition) {
        initParams(image, transition);
    }

    public ImageParams(String path, int transition) {
        initParams(path, transition);
    }

    public ImageParams(ImageData image) {
        initParams(image, -1);
    }

    public ImageParams(String path) {
        initParams(path, -1);
    }

    private void initParams(ImageData image, int transition) {
        this.image = new ImageData(image);
        this.transition = transition;
    }

    private void initParams(String path, int transition) {
        this.image = new ImageData(path);
        this.transition = transition;
    }

    public void paramsFromString(String[] params) {
        initParams(params[0], Integer.valueOf(params[1]));
    }
}
