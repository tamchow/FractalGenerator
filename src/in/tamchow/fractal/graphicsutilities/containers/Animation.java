package in.tamchow.fractal.graphicsutilities.containers;
import java.util.ArrayList;
/**
 * Stores frames for an animation, or here, transitions, with some metadata such as fps.
 */
public class Animation {
    private int fps;
    private ArrayList<PixelContainer> frames;
    public Animation() {
        fps = 24;
        frames = new ArrayList<>();
    }
    public Animation(int fps) {
        this.fps = fps;
        frames = new ArrayList<>();
    }
    public Animation(int fps, PixelContainer[] frames) {
        this.fps = fps;
        this.frames = new ArrayList<>();
        setFrames(frames);
    }
    public int getNumFrames() {
        return frames.size();
    }
    public int getFrameWidth() {
        return getFrameWidth(0);
    }
    public int getFrameWidth(int idx) {
        return frames.get(0).getWidth();
    }
    public int getFrameHeight() {
        return getFrameHeight(0);
    }
    public int getFrameHeight(int idx) {
        return frames.get(idx).getHeight();
    }
    public int getFps() {
        return fps;
    }
    public void setFps(int fps) {
        this.fps = fps;
    }
    public void setFrame(int idx, PixelContainer frame) {
        frames.set(idx, new PixelContainer(frame));
    }
    public void removeFrame(int idx) {
        frames.remove(idx);
    }
    public void removeFrame(PixelContainer frame) {
        frames.remove(frame);
    }
    public PixelContainer[] getFrames() {
        PixelContainer[] tmp = new PixelContainer[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            tmp[i] = getFrame(i);
        }
        return tmp;
    }
    public void setFrames(PixelContainer[] frames) {
        clearFrames();
        for (PixelContainer imgdat : frames) {
            addFrame(imgdat);
        }
    }
    public PixelContainer getFrame(int idx) {
        return frames.get(idx);
    }
    public void addFrame(PixelContainer frame) {
        frames.add(new PixelContainer(frame));
    }
    public void clearFrames() {
        frames.clear();
    }
    @Override
    public String toString() {
        return "" + fps + "," + frames.size();
    }
}