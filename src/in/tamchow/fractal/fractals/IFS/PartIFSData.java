package in.tamchow.fractal.fractals.IFS;
import in.tamchow.fractal.imgutils.containers.ImageData;
/**
 * Part of an IFS fractal's image data
 */
public class PartIFSData {
    ImageData partPlane;
    public PartIFSData(ImageData part) {
        setPartPlane(part);
    }
    public ImageData getPartPlane() {
        return partPlane;
    }
    public void setPartPlane(ImageData partPlane) {
        this.partPlane = new ImageData(partPlane);
    }
}