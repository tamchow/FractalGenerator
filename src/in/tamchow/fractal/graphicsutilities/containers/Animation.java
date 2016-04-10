package in.tamchow.fractal.graphicsutilities.containers;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * Stores frames for an animation, or here, transitions, with some metadata such as fps.
 */
public class Animation implements Serializable, Comparable<Animation> {
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
    public Animation(int fps, @NotNull PixelContainer[] frames) {
        this.fps = fps;
        this.frames = new ArrayList<>();
        setFrames(frames);
    }
    public Animation(@NotNull Animation other) {
        this.fps = other.fps;
        this.frames = new ArrayList<>(other.frames);
    }
    @NotNull
    @Override
    public String toString() {
        return fps + " " + frames.toString();
    }
    public void addFrames(@NotNull Animation frames) {
        for (int i = 0; i < frames.getNumFrames(); ++i) {
            this.frames.add(frames.getFrame(i));
        }
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
    @NotNull
    public PixelContainer[] getFrames() {
        @NotNull PixelContainer[] tmp = new PixelContainer[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            tmp[i] = getFrame(i);
        }
        return tmp;
    }
    public void setFrames(@NotNull PixelContainer[] frames) {
        clearFrames();
        for (PixelContainer pixelContainer : frames) {
            addFrame(pixelContainer);
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
    @NotNull
    public String sizeDataString() {
        return "" + fps + "," + frames.size();
    }
    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof Animation && ((Animation) o).fps == fps && ((Animation) o).frames.equals(frames));
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    @Override
    public int compareTo(@NotNull Animation o) {
        int difference = 0;
        for (int i = 0; i < getNumFrames() && i < o.getNumFrames(); ++i) {
            difference += getFrame(i).compareTo(o.getFrame(i));
        }
        return difference ^ (getFps() - o.getFps());
    }
}