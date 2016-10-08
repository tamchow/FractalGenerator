package in.tamchow.fractal.graphics.transition;
import in.tamchow.fractal.color.Colorizer;
import in.tamchow.fractal.color.Colors;
import in.tamchow.fractal.graphics.containers.Animation;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * Creates transitions between images.
 * Note: Images to be transitioned between must be of the same resolution. No scaling is implemented here.
 */
public final class Transition {
    private PixelContainer img1, img2;
    private Animation frames;
    private TransitionType transtype;
    private int transtime;
    public Transition(TransitionType transtype, @NotNull PixelContainer img1, @NotNull PixelContainer img2, int fps, int time) {
        setImg1(img1);
        setImg2(img2);
        frames = new Animation(fps);
        this.transtype = transtype;
        transtime = time;
    }
    public int getTranstime() {
        return transtime;
    }
    public void setTranstime(int transtime) {
        this.transtime = transtime;
    }
    public Animation getFrames() {
        return frames;
    }
    public void setFrames(@NotNull PixelContainer[] frames) {
        this.frames.setFrames(frames);
    }
    public void setFrames(Animation frames) {
        this.frames = frames;
    }
    public PixelContainer getImg2() {
        return img2;
    }
    public void setImg2(@NotNull PixelContainer img2) {
        this.img2 = new PixelContainer(img2);
    }
    public PixelContainer getImg1() {
        return img1;
    }
    public void setImg1(@NotNull PixelContainer img1) {
        this.img1 = new PixelContainer(img1);
    }
    public TransitionType getTranstype() {
        return transtype;
    }
    public void setTranstype(TransitionType transtype) {
        this.transtype = transtype;
    }
    public void doTransition() {
        frames.clearFrames();
        @NotNull PixelContainer tmp = new PixelContainer(img1);
        @Nullable int[][] pixdata = tmp.getPixdata();
        frames.addFrame(img1);
        int numframes = frames.getFps() * transtime;
        int bandwidth = img2.getWidth() / numframes;
        int bandheight = img2.getHeight() / numframes;
        switch (transtype) {
            case TOP:
                for (int i = 0; i < img1.getHeight() - bandheight; i += bandheight) {
                    for (int j = 0; j < bandheight; j++) {
                        for (int k = 0; k < img2.getWidth(); k++) {
                            tmp.setPixel(i + j, k, img2.getPixel(i + j, k));
                        }
                    }
                    frames.addFrame(tmp);
                }
                frames.addFrame(img2);
                break;
            case BOTTOM:
                for (int i = img1.getHeight() - 1; i >= bandheight; i -= bandheight) {
                    for (int j = bandheight - 1; j >= 0; j--) {
                        for (int k = 0; k < img2.getWidth(); k++) {
                            tmp.setPixel(i - j, k, img2.getPixel(i - j, k));
                        }
                    }
                    frames.addFrame(tmp);
                }
                frames.addFrame(img2);
                break;
            case LEFT:
                for (int i = 0; i < img1.getWidth() - bandwidth; i += bandwidth) {
                    for (int j = 0; j < bandwidth; j++) {
                        for (int k = 0; k < img2.getHeight(); k++) {
                            tmp.setPixel(k, i + j, img2.getPixel(k, i + j));
                        }
                    }
                    frames.addFrame(tmp);
                }
                frames.addFrame(img2);
                break;
            case RIGHT:
                for (int i = img1.getWidth() - 1; i >= bandwidth; i -= bandwidth) {
                    for (int j = bandwidth - 1; j >= 0; j--) {
                        for (int k = 0; k < img2.getHeight(); k++) {
                            tmp.setPixel(k, i - j, img2.getPixel(k, i - j));
                        }
                    }
                    frames.addFrame(tmp);
                }
                frames.addFrame(img2);
                break;
            case CENTRE_OUT:
                for (int i = img2.getHeight() / 2, j = img2.getHeight() / 2; i >= 0 && j < img2.getHeight(); i -= bandheight, j += bandheight) {
                    for (int k = img2.getWidth() / 2, l = img2.getWidth() / 2; k >= 0 && l < img2.getWidth(); k -= bandwidth, l += bandwidth) {
                        for (int m = i; m <= j; m++) {
                            System.arraycopy(img2.getRow(m), k, pixdata[m], k, l);
                        }
                    }
                    tmp.setPixdata(pixdata);
                    frames.addFrame(tmp);
                }
                frames.addFrame(img2);
                break;
            case EDGE_IN:
                for (int i = img2.getHeight() - 1, j = 0; i >= img2.getHeight() / 2 && j < img2.getHeight() / 2; i -= bandheight, j += bandheight) {
                    for (int k = img2.getWidth() - 1, l = 0; k >= img2.getWidth() / 2 && l < img2.getWidth() / 2; k -= bandwidth, l += bandwidth) {
                        for (int m = i; m <= j; m++) {
                            System.arraycopy(img2.getRow(m), k, pixdata[m], k, l);
                        }
                    }
                    tmp.setPixdata(pixdata);
                    frames.addFrame(tmp);
                }
                frames.addFrame(img2);
                break;
            case NONE:
                frames.addFrame(img2);
                break;
            case CROSSFADE:
                frames.clearFrames();
                for (int i = 0; i <= numframes; i++) {
                    for (int j = 0; j < img2.getHeight(); j++) {
                        for (int k = 0; k < img2.getWidth(); k++) {
                            int r = (int) (Colorizer.separateARGB(img2.getPixel(j, k), Colors.RGBCOMPONENTS.RED) * ((double) i / numframes) + Colorizer.separateARGB(img1.getPixel(j, k), Colors.RGBCOMPONENTS.RED) * (1 - ((double) i / numframes)));
                            int g = (int) (Colorizer.separateARGB(img2.getPixel(j, k), Colors.RGBCOMPONENTS.GREEN) * ((double) i / numframes) + Colorizer.separateARGB(img1.getPixel(j, k), Colors.RGBCOMPONENTS.GREEN) * (1 - ((double) i / numframes)));
                            int b = (int) (Colorizer.separateARGB(img2.getPixel(j, k), Colors.RGBCOMPONENTS.BLUE) * ((double) i / numframes) + Colorizer.separateARGB(img1.getPixel(j, k), Colors.RGBCOMPONENTS.BLUE) * (1 - ((double) i / numframes)));
                            tmp.setPixel(j, k, Colorizer.packRGB(r, g, b));
                        }
                    }
                    frames.addFrame(tmp);
                }
                break;
            default:
                throw new IllegalArgumentException("Unrecognized transition type");
        }
    }
}