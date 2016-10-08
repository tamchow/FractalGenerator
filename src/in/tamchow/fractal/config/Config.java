package in.tamchow.fractal.config;
import java.io.Serializable;

import static in.tamchow.fractal.config.Strings.CONFIG_SEPARATOR;
/**
 * Superclass for set configurations
 */
public class Config implements Serializable, DataFromString {
    protected int height;
    protected int width;
    protected int wait;
    protected int fps;
    protected String name, path;
    {
        setWait(0);
        setFps(0);
    }
    public Config(int height, int width, int wait, int fps, String path) {
        setHeight(height);
        setWidth(width);
        setWait(wait);
        setFps(fps);
        setPath(path);
    }
    public Config(int height, int width, String path) {
        this(height, width, 0, 0, path);
    }
    public Config() {
        this(0, 0, "");
    }
    public int getFps() {
        return fps;
    }
    public void setFps(int fps) {
        this.fps = (fps < 0) ? 0 : fps;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height < 0 ? 0 : height;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width < 0 ? 0 : width;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        if (this.name == null || this.name.isEmpty()) {
            this.name = name;
        }
    }
    public int getWait() {
        return wait;
    }
    public void setWait(int wait) {
        this.wait = wait;
    }
    public void fromString(String[] config) {
        //setName(config[0]);
        setPath(config[0]);
        setHeight(Integer.valueOf(config[1]));
        setWidth(Integer.valueOf(config[2]));
        setWait(Integer.valueOf(config[3]));
        setFps(Integer.valueOf(config[4]));
    }
    @Override
    public String toString() {
        return path + CONFIG_SEPARATOR + height + CONFIG_SEPARATOR + width + CONFIG_SEPARATOR + wait + CONFIG_SEPARATOR + fps;
    }
}