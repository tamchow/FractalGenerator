package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
/**
 * Part of an IFS fractal's image data
 */
public class PartIFSData {
    PixelContainer partPlane;
    public PartIFSData(PixelContainer part) {
        setPartPlane(part);
    }
    public PixelContainer getPartPlane() {
        return partPlane;
    }
    public void setPartPlane(PixelContainer partPlane) {
        this.partPlane = new PixelContainer(partPlane);
    }
}