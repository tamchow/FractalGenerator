package in.tamchow.fractal.imgutils;
import java.util.ArrayList;
/**
 * Stores frames for an animation, or here, transitions, with some metadata such as fps.
 */
public class Animation {
    private int fps;
    private ArrayList<ImageData> frames;
    public Animation() {fps = 24; frames = new ArrayList<>();}
    public Animation(int fps) {this.fps = fps; frames = new ArrayList<>();}
    public Animation(int fps, ImageData[] frames) {this.fps = fps; this.frames = new ArrayList<>(); setFrames(frames);}
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
    public void setFrame(int idx, ImageData frame) {
        frames.set(idx, new ImageData(frame));
    }
    public void removeFrame(int idx) {frames.remove(idx);}
    public void removeFrame(ImageData frame) {frames.remove(frame);}
    public ImageData[] getFrames() {
        ImageData[] tmp = new ImageData[frames.size()]; for (int i = 0; i < frames.size(); i++) {tmp[i] = getFrame(i);}
        return tmp;}
    public ImageData getFrame(int idx) {return frames.get(idx);}
    public void setFrames(ImageData[] frames) {
        clearFrames(); for (ImageData imgdat : frames) {addFrame(imgdat);}}
    public void addFrame(ImageData frame) {frames.add(new ImageData(frame));}
    public void clearFrames() {frames.clear();}
    @Override
    public String toString() {return "" + fps + "," + frames.size();}
}