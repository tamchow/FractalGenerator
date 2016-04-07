package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
/**
 * Part of an IFS fractal's image data
 */
public class PartIFSData {
    PixelContainer partPlane;
    Animation partAnimation;
    public PartIFSData(PixelContainer partPlane, Animation partAnimation) {
        setPartPlane(partPlane);
        setPartAnimation(partAnimation);
    }
    public PixelContainer getPartPlane() {
        return partPlane;
    }
    public void setPartPlane(PixelContainer partPlane) {
        this.partPlane = new PixelContainer(partPlane);
    }
    public Animation getPartAnimation() {
        return partAnimation;
    }
    public void setPartAnimation(Animation partAnimation) {
        this.partAnimation = new Animation(partAnimation);
    }
}