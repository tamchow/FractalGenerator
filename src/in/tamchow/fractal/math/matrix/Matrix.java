package in.tamchow.fractal.math.matrix;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import java.io.Serializable;
/**
 * Holds a rectangular matrix
 */
public final class Matrix extends Number implements Serializable, Comparable<Matrix> {
    private int rows, columns;
    private double[][] matrixData;
    public Matrix(double[][] matrixData) {
        initMatrix(matrixData.length, matrixData[0].length, matrixData);
    }
    public Matrix(Matrix old) {
        initMatrix(old.getNumRows(), old.getNumColumns(), old.getMatrixData());
    }
    public Matrix(int rows, int columns) {
        setNumRows(rows);
        setNumColumns(columns);
        matrixData = new double[this.rows][this.columns];
    }
    public Matrix(String matrix) {
        matrix = matrix.substring(1, matrix.length() - 1);//trim leading and trailing square brackets
        String[] rows = StringManipulator.split(matrix, ";");
        this.rows = rows.length;
        this.columns = StringManipulator.split(rows[0].substring(1, rows[0].length() - 1), ",").length;
        for (int i = 0; i < matrixData.length && i < rows.length; i++) {
            //trim leading and trailing square brackets
            String[] columns = StringManipulator.split(rows[i].substring(1, rows[i].length() - 1), ",");
            for (int j = 0; j < matrixData[i].length && j < columns.length; j++) {
                matrixData[i][j] = Double.valueOf(columns[j]);
            }
        }
    }
    public static Matrix rotationMatrix2D(double angle) {
        return new Matrix(new double[][]{{Math.cos(angle), -Math.sin(angle)}, {Math.sin(angle), Math.cos(angle)}});
    }
    public static Matrix nullMatrix(int order) {
        return nullMatrix(order, order);
    }
    public static Matrix nullMatrix(int rows, int colums) {
        return new Matrix(rows, colums);
    }
    public static Matrix identityMatrix(int order) {
        int rows = Math.round((float) Math.sqrt(order));
        //Note: For an identity matrix, rows=columns, so we reuse `rows` as `columns`
        Matrix matrix = new Matrix(rows, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows; j++) {
                if (i == j) {
                    matrix.set(i, j, 1);
                } else {
                    matrix.set(i, j, 0);
                }
            }
        }
        return matrix;
    }
    private void initMatrix(int rows, int columns, double[][] matrixData) {
        setMatrixData(matrixData);
        setNumRows(rows);
        setNumColumns(columns);
    }
    public int getNumRows() {
        return rows;
    }
    public void setNumRows(int rows) {
        this.rows = rows;
    }
    public int getNumColumns() {
        return columns;
    }
    public void setNumColumns(int columns) {
        this.columns = columns;
    }
    public double[][] getMatrixData() {
        return matrixData;
    }
    public void setMatrixData(double[][] matrixData) {
        this.matrixData = new double[matrixData.length][matrixData[0].length];
        for (int i = 0; i < matrixData.length; i++) {
            System.arraycopy(matrixData[i], 0, this.matrixData[i], 0, matrixData[i].length);
        }
    }
    public synchronized void set(int i, int j, double value) {
        matrixData[i][j] = value;
    }
    public Matrix transpose() {
        Matrix transposedMatrix = new Matrix(getNumColumns(), getNumRows());
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
    public Matrix createSubMatrix(int excluding_row, int excluding_col) {
        Matrix mat = new Matrix(getNumRows() - 1, getNumColumns() - 1);
        int r = -1;
        for (int i = 0; i < getNumRows(); i++) {
            if (i == excluding_row) continue;
            r++;
            int c = -1;
            for (int j = 0; j < getNumColumns(); j++) {
                if (j == excluding_col) continue;
                mat.set(r, ++c, get(i, j));
            }
        }
        return mat;
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
    public double determinant(Matrix matrix) {
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
    public Matrix cofactor() {
        Matrix mat = new Matrix(getNumRows(), getNumColumns());
        for (int i = 0; i < getNumRows(); i++) {
            for (int j = 0; j < getNumColumns(); j++) {
                mat.set(i, j, changeSign(i) * changeSign(j) * determinant(createSubMatrix(i, j)));
            }
        }
        return mat;
    }
    @Override
    public synchronized boolean equals(Object that) {
        if (that == null || (!(that instanceof Matrix))) {
            return false;
        }
        Matrix other = (Matrix) that;
        if (!(getNumColumns() == other.getNumColumns() && getNumRows() == other.getNumRows())) return false;
        for (int i = 0; i < other.getNumRows(); i++) {
            for (int j = 0; j < other.getNumColumns(); j++) {
                if (matrixData[i][j] != other.getMatrixData()[i][j]) return false;
            }
        }
        return true;
    }
    @Override
    public String toString() {
        String matrix = "";
        for (double[] aMatrixData : matrixData) {
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
    public int compareTo(Matrix other) {
        if (other.getNumColumns() + other.getNumRows() != getNumColumns() + getNumRows()) {
            return (other.getNumColumns() + other.getNumRows()) - (getNumColumns() + getNumRows());
        } else {
            //Representation-based lexicographical comparison - makes not much mathematical sense
            return toString().compareTo(other.toString());
        }
    }
    private double sumAllElements() {
        double sum = 0;
        for (double[] row : matrixData) {
            for (double element : row) {
                sum += element;
            }
        }
        return sum;
    }
    @Override
    public int hashCode() {
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