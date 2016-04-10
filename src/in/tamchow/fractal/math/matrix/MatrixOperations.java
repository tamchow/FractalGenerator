package in.tamchow.fractal.math.matrix;
import org.jetbrains.annotations.NotNull;

import java.util.InputMismatchException;
/**
 * Elementary matrix operations: Addition, subtraction, scalar multiplication, matrix multiplication
 */
public class MatrixOperations {
    @NotNull
    public static Matrix add(@NotNull Matrix m1, @NotNull Matrix m2) {
        if (m1.getNumRows() != m2.getNumRows() || m1.getNumColumns() != m2.getNumColumns()) {
            throw new InputMismatchException("Given Matrices have different orders, cannot be added.");
        }
        @NotNull double[][] added = new double[m1.getNumRows()][m1.getNumColumns()];
        for (int i = 0; i < m1.getNumRows(); i++) {
            for (int j = 0; j < m1.getNumColumns(); j++) {
                added[i][j] = m1.get(i, j) + m2.get(i, j);
            }
        }
        return new Matrix(added);
    }
    @NotNull
    public static Matrix subtract(@NotNull Matrix m1, @NotNull Matrix m2) {
        if (m1.getNumRows() != m2.getNumRows() || m1.getNumColumns() != m2.getNumColumns()) {
            throw new InputMismatchException("Given Matrices have different orders, cannot be subtracted.");
        }
        @NotNull double[][] minused = new double[m1.getNumRows()][m1.getNumColumns()];
        for (int i = 0; i < m1.getNumRows(); i++) {
            for (int j = 0; j < m1.getNumColumns(); j++) {
                minused[i][j] = m1.get(i, j) - m2.get(i, j);
            }
        }
        return new Matrix(minused);
    }
    @NotNull
    public static Matrix multiply(@NotNull Matrix m1, double scalar) {
        @NotNull double[][] scalared = new double[m1.getNumRows()][m1.getNumColumns()];
        for (int i = 0; i < m1.getNumRows(); i++) {
            for (int j = 0; j < m1.getNumColumns(); j++) {
                scalared[i][j] = scalar * m1.get(i, j);
            }
        }
        return new Matrix(scalared);
    }
    @NotNull
    public static Matrix power(@NotNull Matrix matrix, int power) {
        if (power == 0) {
            return Matrix.identityMatrix(matrix.size());
        }
        if (power < 0) {
            matrix = matrix.inverse();
            power = -power;
        }
        for (int i = 1; i <= power; i++) {
            matrix = multiply(matrix, matrix);
        }
        return matrix;
    }
    @NotNull
    public static Matrix multiply(@NotNull Matrix m1, @NotNull Matrix m2) {
        double sum;
        if (m1.getNumColumns() != m2.getNumRows()) {
            throw new InputMismatchException("Matrices with entered orders can't be multiplied with each other.");
        }
        @NotNull double[][] product = new double[m1.getNumRows()][m2.getNumColumns()];
        for (int i = 0; i < m1.getNumRows(); i++) {
            for (int j = 0; j < m2.getNumColumns(); j++) {
                sum = 0;
                for (int k = 0; k < m2.getNumRows(); k++)
                    sum = sum + m1.get(i, k) * m2.get(k, j);
                product[i][j] = sum;
            }
        }
        return new Matrix(product);
    }
}