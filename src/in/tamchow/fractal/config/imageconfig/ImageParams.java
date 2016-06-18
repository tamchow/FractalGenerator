package in.tamchow.fractal.config.imageconfig;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.transition.TransitionTypes;
import in.tamchow.fractal.helpers.annotations.NotNull;

import static in.tamchow.fractal.config.Strings.BLOCKS;
import static in.tamchow.fractal.config.Strings.CONFIG_SEPARATOR;
/**
 * Parameters for configuring an image
 *
 * NOTE: For auto-sizing ima
 */
public class ImageParams extends Config {
    private PixelContainer image;
    private TransitionTypes transition;
    private int transtime;
    {
        setName(BLOCKS.IMAGE);
    }
    public ImageParams() {
        super();
    }
    public ImageParams(@NotNull ImageParams old) {
        super(old.height, old.width, old.wait, old.fps, old.path);
        initParams(old.transtime, old.transition);
        this.image = new PixelContainer(old.image);
    }
    public ImageParams(int width, int height, String path, int transtime, int fps, int wait, @NotNull PixelContainer image, TransitionTypes transition) {
        super(height, width, wait, fps, path);
        initParams(transtime, transition);
        this.image = new PixelContainer(image);
    }
    public ImageParams(int width, int height, String path, int transtime, int fps, int wait, TransitionTypes transition) {
        super(height, width, wait, fps, path);
        initParams(transtime, transition);
        this.image = new PixelContainer(path);
    }
    public ImageParams(int width, int height, String path, int transtime, int fps, int wait, @NotNull PixelContainer image) {
        this(width, height, path, transtime, fps, wait, image, TransitionTypes.NONE);
    }
    public ImageParams(int width, int height, int transtime, int fps, int wait, String path) {
        this(width, height, path, transtime, fps, wait, TransitionTypes.NONE);
    }
    public PixelContainer getImage() {
        return image;
    }
    public TransitionTypes getTransition() {
        return transition;
    }
    public int getTranstime() {
        return transtime;
    }
    public void setTranstime(int transtime) {
        this.transtime = transtime;
    }
    public boolean customDimensions() {
        return height <= 0 || width <= 0;
    }
    private void initParams(int transtime, TransitionTypes transition) {
        setTranstime(transtime);
        this.transition = transition;
    }
    @NotNull
    public String toString() {
        return super.toString() + transtime + CONFIG_SEPARATOR + transition;
    }
    public void fromString(@NotNull String params) {
        fromString(params.split(CONFIG_SEPARATOR));
    }
    public void fromString(String[] params) {
        super.fromString(params);
        initParams(Integer.valueOf(params[5]), TransitionTypes.valueOf(params[6]));
        this.image = new PixelContainer(path);
    }
}