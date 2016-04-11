package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Part of an IFS fractal's image data
 */
public class PartIFSData {
    PixelContainer partPlane;
    Animation partAnimation;
    public PartIFSData(@NotNull PixelContainer partPlane, @NotNull Animation partAnimation) {
        setPartPlane(partPlane);
        setPartAnimation(partAnimation);
    }
    public PixelContainer getPartPlane() {
        return partPlane;
    }
    public void setPartPlane(@NotNull PixelContainer partPlane) {
        this.partPlane = new PixelContainer(partPlane);
    }
    public Animation getPartAnimation() {
        return partAnimation;
    }
    public void setPartAnimation(@NotNull Animation partAnimation) {
        this.partAnimation = new Animation(partAnimation);
    }
}