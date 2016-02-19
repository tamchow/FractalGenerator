package in.tamchow.fractal.fractals.complexbrot;
import java.io.Serializable;
/**
 * Holds a part of a fractal's data for threaded generation.
 */
public class PartComplexBrotFractalData implements Serializable {
    int[][] base, escapedata;
    double[][] normalized_escapes;
    public PartComplexBrotFractalData(int[][] base, int[][] escapedata, double[][] normalized_escapes) {
        setBase(base); setEscapedata(escapedata); setNormalized_escapes(normalized_escapes);
    }
    public int[][] getBase() {return base;}
    public void setBase(int[][] base) {
        this.base = new int[base.length][base[0].length]; for (int i = 0; i < this.base.length; i++) {
            System.arraycopy(base[i], 0, this.base[i], 0, this.base[i].length);
        }
    }
    public int[][] getEscapedata() {return escapedata;}
    public void setEscapedata(int[][] escapedata) {
        this.escapedata = new int[escapedata.length][escapedata[0].length];
        for (int i = 0; i < this.escapedata.length; i++) {
            System.arraycopy(escapedata[i], 0, this.escapedata[i], 0, this.escapedata[i].length);
        }
    }
    public double[][] getNormalized_escapes() {return normalized_escapes;}
    public void setNormalized_escapes(double[][] normalized_escapes) {
        this.normalized_escapes = new double[normalized_escapes.length][normalized_escapes[0].length];
        for (int i = 0; i < this.normalized_escapes.length; i++) {
            System.arraycopy(normalized_escapes[i], 0, this.normalized_escapes[i], 0, this.normalized_escapes[i].length);
        }
    }
}