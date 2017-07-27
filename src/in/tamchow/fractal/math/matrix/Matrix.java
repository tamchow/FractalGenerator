package in.tamchow.fractal.math.matrix;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import java.io.Serializable;
/**
 * Holds a rectangular matrix
 */
public final class Matrix extends Number implements Serializable, Comparable<Matrix>, Cloneable {
    private int rows, columns;
    private double[][] matrixData;
    public Matrix(@NotNull double[][] matrixData) {
        initMatrix(matrixData.length, matrixData[0].length, matrixData);
    }
    public Matrix(@NotNull Matrix old) {
        initMatrix(old.getNumRows(), old.getNumColumns(), old.getMatrixData());
    }
    public Matrix(int rows, int columns) {
        initMatrix(rows, columns, new double[rows][columns]);
    }
    public Matrix(String matrix) {
        matrix = matrix.substring(1, matrix.length() - 1);//trim leading and trailing square brackets
        @NotNull String[] rows = StringManipulator.split(matrix, ";");
        this.rows = rows.length;
        this.columns = StringManipulator.split(rows[0].substring(1, rows[0].length() - 1), ",").length;
        for (int i = 0; i < matrixData.length && i < rows.length; i++) {
            //trim leading and trailing square brackets
            @NotNull String[] columns = StringManipulator.split(rows[i].substring(1, rows[i].length() - 1), ",");
            for (int j = 0; j < matrixData[i].length && j < columns.length; j++) {
                matrixData[i][j] = Double.parseDouble(columns[j]);
            }
        }
    }
    @NotNull
    public static Matrix rotationMatrix2D(double angle) {
        return new Matrix(new double[][]{{Math.cos(angle), Math.sin(angle)}, {-Math.sin(angle), Math.cos(angle)}});
    }
    @NotNull
    public static Matrix nullMatrix(int order) {
        return nullMatrix(order, order);
    }
    @NotNull
    public static Matrix nullMatrix(int rows, int columns) {
        return new Matrix(rows, columns);
    }
    @NotNull
    public static Matrix identityMatrix(int order) {
        double[][] data = new double[order][order];
        for (int i = 0; i < order; ++i) {
            for (int j = 0; j < order; ++j) {
                data[i][j] = (i == j) ? 1 : 0;
            }
        }
        return new Matrix(data);
    }
    @Override
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        return new Matrix(this);
    }
    private void initMatrix(int rows, int columns, @NotNull double[][] matrixData) {
        setMatrixData(matrixData);
        setNumRows(rows);
        setNumColumns(columns);
    }
    public int getNumRows() {
        return rows;
    }
    private void setNumRows(int rows) {
        this.rows = rows;
    }
    public int getNumColumns() {
        return columns;
    }
    private void setNumColumns(int columns) {
        this.columns = columns;
    }
    public double[][] getMatrixData() {
        return matrixData;
    }
    private void setMatrixData(@NotNull double[][] matrixData) {
        this.matrixData = new double[matrixData.length][matrixData[0].length];
        for (int i = 0; i < matrixData.length; i++) {
            System.arraycopy(matrixData[i], 0, this.matrixData[i], 0, matrixData[i].length);
        }
    }
    public Matrix set(int i, int j, double value) {
        matrixData[i][j] = value;
        return this;
    }
    @NotNull
    public Matrix transpose() {
        @NotNull Matrix transposedMatrix = new Matrix(getNumColumns(), getNumRows());
        for (int i = 0; i < getNumRows(); i++) {
            for (int j = 0; j < getNumColumns(); j++) {
                transposedMatrix.set(j, i, get(i, j));
            }
        }
        return transposedMatrix;
    }
    public boolean isSquare() {
        return rows == columns;
    }
    @NotNull
    public Matrix createSubMatrix(int excluding_row, int excluding_col) {
        double[][] data = new double[getNumRows() - 1][getNumColumns() - 1];
        int r = -1;
        for (int i = 0; i < getNumRows(); i++) {
            if (i == excluding_row) continue;
            r++;
            int c = -1;
            for (int j = 0; j < getNumColumns(); j++) {
                if (j == excluding_col) continue;
                data[r][++c] = get(i, j);
            }
        }
        return new Matrix(data);
    }
    public int size() {
        return rows * columns;
    }
    private int changeSign(int val) {
        if (val % 2 == 0) {
            return 1;
        }
        return -1;
    }
    public double determinant() {
        return determinant(this);
    }
    public double determinant(@NotNull Matrix matrix) {
        if (!matrix.isSquare()) throw new IllegalArgumentException("Matrix needs to be square.");
        if (matrix.size() == 1) {
            return matrix.get(0, 0);
        }
        if (matrix.size() == 2) {
            return (matrix.get(0, 0) * matrix.get(1, 1)) - (matrix.get(0, 1) * matrix.get(1, 0));
        }
        double sum = 0.0;
        for (int i = 0; i < matrix.getNumColumns(); i++) {
            sum += changeSign(i) * get(0, i) * determinant(createSubMatrix(0, i));
        }
        return sum;
    }
    public Matrix inverse() {
        return MatrixOperations.multiply(cofactor().transpose(), 1.0 / determinant());
    }
    @NotNull
    public Matrix cofactor() {
        double[][] data = new double[getNumRows()][getNumColumns()];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = changeSign(i) * changeSign(j) * determinant(createSubMatrix(i, j));
            }
        }
        return new Matrix(data);
    }
    @Override
    public boolean equals(@Nullable Object that) {
        if (that == this) {
            return true;
        }
        if (that == null || (!(that instanceof Matrix))) {
            return false;
        }
        @Nullable Matrix other = (Matrix) that;
        if (!(getNumColumns() == other.getNumColumns() && getNumRows() == other.getNumRows())) return false;
        for (int i = 0; i < other.getNumRows(); i++) {
            for (int j = 0; j < other.getNumColumns(); j++) {
                if (matrixData[i][j] != other.getMatrixData()[i][j]) return false;
            }
        }
        return true;
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String matrix = "";
        for (@NotNull double[] aMatrixData : matrixData) {
            for (double anAMatrixData : aMatrixData) {
                matrix += anAMatrixData + ",";
            }
            matrix = "[" + matrix.substring(0, matrix.length() - 1) + "];";
        }//remove trailing ','}
        return "[" + matrix.substring(0, matrix.length() - 1) + "]";//remove trailing ';'
    }
    public synchronized double get(int i, int j) {
        return matrixData[i][j];
    }
    @Override
    public int compareTo(@NotNull Matrix other) {
        if (equals(other)) {
            return 0;
        }
        if (other.getNumColumns() + other.getNumRows() != getNumColumns() + getNumRows()) {
            return (other.getNumColumns() + other.getNumRows()) - (getNumColumns() + getNumRows());
        } else {
            //Representation-based lexicographical comparison - makes not much mathematical sense
            return toString().compareTo(other.toString());
        }
    }
    private double sumAllElements() {
        double sum = 0;
        for (@NotNull double[] row : matrixData) {
            for (double element : row) {
                sum += element;
            }
        }
        return sum;
    }
    @Override
    public int hashCode() {
        //return java.util.Arrays.deepHashCode(matrixData);
        return toString().hashCode();
    }
    @Override
    public int intValue() {
        return isSquare() ? Math.round((float) determinant()) : Math.round((float) sumAllElements());
    }
    @Override
    public long longValue() {
        return isSquare() ? Math.round(determinant()) : Math.round(sumAllElements());
    }
    @Override
    public float floatValue() {
        return isSquare() ? (float) determinant() : (float) sumAllElements();
    }
    @Override
    public double doubleValue() {
        return isSquare() ? determinant() : sumAllElements();
    }
}