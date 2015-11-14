package in.tamchow.fractal.math.matrix;

import java.util.InputMismatchException;

/**
 * Elementary matrix operations: Addition, subtraction, scalar multiplication, matrix multiplication
 */
public class MatrixOperations {
    public static Matrix add(Matrix m1, Matrix m2) {
        if (m1.getRows() != m2.getRows() || m1.getColumns() != m2.getColumns()) {
            throw new InputMismatchException("Given Matrices have different orders, cannot be added.");
        }
        double[][] added = new double[m1.getRows()][m1.getColumns()];
        for (int i = 0; i < m1.getRows(); i++) {
            for (int j = 0; j < m1.getColumns(); j++) {
                added[i][j] = m1.get(i, j) + m2.get(i, j);
            }
        }
        return new Matrix(added);
    }

    public static Matrix subtract(Matrix m1, Matrix m2) {
        if (m1.getRows() != m2.getRows() || m1.getColumns() != m2.getColumns()) {
            throw new InputMismatchException("Given Matrices have different orders, cannot be subtracted.");
        }
        double[][] minused = new double[m1.getRows()][m1.getColumns()];
        for (int i = 0; i < m1.getRows(); i++) {
            for (int j = 0; j < m1.getColumns(); j++) {
                minused[i][j] = m1.get(i, j) - m2.get(i, j);
            }
        }
        return new Matrix(minused);
    }

    public static Matrix multiply(Matrix m1, double scalar) {
        double[][] scalared = new double[m1.getRows()][m1.getColumns()];
        for (int i = 0; i < m1.getRows(); i++) {
            for (int j = 0; j < m1.getColumns(); j++) {
                scalared[i][j] = scalar * m1.get(i, j);
            }
        }
        return new Matrix(scalared);
    }

    public static Matrix multiply(Matrix m1, Matrix m2) {
        double sum;

        if (m1.getColumns() != m2.getRows()) {
            throw new InputMismatchException("Matrices with entered orders can't be multiplied with each other.");
        }
        double[][] product = new double[m1.getRows()][m2.getColumns()];
        for (int i = 0; i < m1.getRows(); i++) {
            for (int j = 0; j < m2.getColumns(); j++) {
                sum = 0;
                for (int k = 0; k < m2.getRows(); k++)
                    sum = sum + m1.get(i, k) * m2.get(k, j);
                product[i][j] = sum;
            }
        }

        return new Matrix(product);
    }
}
