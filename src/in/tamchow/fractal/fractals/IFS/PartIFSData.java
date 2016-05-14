package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Part of an IFS fractal's image data
 */
public class PartIFSData {
    private PixelContainer partPlane;
    private Animation partAnimation;
    private double[][] partWeightData;
    public PartIFSData(@NotNull PixelContainer partPlane, @NotNull Animation partAnimation, @NotNull double[][] partWeightData) {
        setPartPlane(partPlane);
        setPartAnimation(partAnimation);
        setPartWeightData(partWeightData);
    }
    double[][] getPartWeightData() {
        return partWeightData;
    }
    private void setPartWeightData(double[][] partWeightData) {
        this.partWeightData = new double[partWeightData.length][partWeightData[0].length];
        for (int i = 0; i < partWeightData.length; ++i) {
            System.arraycopy(partWeightData[i], 0, this.partWeightData[i], 0, partWeightData[i].length);
        }
    }
    PixelContainer getPartPlane() {
        return partPlane;
    }
    private void setPartPlane(@NotNull PixelContainer partPlane) {
        this.partPlane = new PixelContainer(partPlane);
    }
    Animation getPartAnimation() {
        return partAnimation;
    }
    private void setPartAnimation(@NotNull Animation partAnimation) {
        this.partAnimation = new Animation(partAnimation);
    }
}