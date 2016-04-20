package in.tamchow.fractal.fractals.complexbrot;
import in.tamchow.fractal.helpers.annotations.NotNull;

import java.io.Serializable;
/**
 * Holds a part of a fractal's data for threaded generation.
 */
public class PartComplexBrotFractalData implements Serializable {
    private int[][][] bases;
    private int discardedPoints;
    public PartComplexBrotFractalData(@NotNull int[][][] bases, int discardedPoints) {
        setBase(bases);
        this.discardedPoints = discardedPoints;
    }
    public void setBase(@NotNull int[][][] bases) {
        this.bases = new int[bases.length][bases[0].length][bases[0][0].length];
        for (int i = 0; i < this.bases.length; i++) {
            for (int j = 0; j < this.bases[i].length; j++) {
                System.arraycopy(bases[i][j], 0, this.bases[i][j], 0, this.bases[i][j].length);
            }
        }
    }
    public int[][][] getBases() {
        return bases;
    }
    public int getDiscardedPoints() {
        return discardedPoints;
    }
}