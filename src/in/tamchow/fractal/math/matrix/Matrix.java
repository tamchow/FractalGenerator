package in.tamchow.fractal.math.matrix;
import java.io.Serializable;
/**
 * Holds a rectangular matrix
 */
public final class Matrix implements Serializable, Comparable<Matrix> {
    private int rows, columns;
    private double[][] matrixData;
    public Matrix(double[][] matrixData) {initMatrix(matrixData.length, matrixData[0].length, matrixData);}
    private void initMatrix(int rows, int columns, double[][] matrixData) {
        setMatrixData(matrixData);
        setNumRows(rows);
        setNumColumns(columns);
    }
    public Matrix(Matrix old) {initMatrix(old.getNumRows(), old.getNumColumns(), old.getMatrixData());}
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
    public Matrix(int rows, int columns) {
        setNumRows(rows); setNumColumns(columns); matrixData = new double[this.rows][this.columns];
    }
    public static Matrix rotationMatrix2D(double angle) {
        double[][] matrixData = new double[2][2]; matrixData[0][0] = Math.cos(angle);
        matrixData[0][1] = -Math.sin(angle); matrixData[1][0] = Math.sin(angle); matrixData[1][1] = Math.cos(angle);
        return new Matrix(matrixData);
    }
    public static Matrix identityMatrix(int order) {
        int rows = Math.round((float) Math.sqrt(order)), columns = rows; Matrix matrix = new Matrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (i == j) {
                    matrix.set(i, j, 1);
                } else {matrix.set(i, j, 0);}
            }
        } return matrix;
    }
    public synchronized void set(int i, int j, double value) {
        matrixData[i][j] = value;
    }
    public static Matrix fromString(String matrix) {
        matrix = matrix.substring(1, matrix.length() - 1);//trim leading and trailing square brackets
        String[] rows = matrix.split(";"); int nrow = rows.length;
        int ncolumn = rows[0].substring(1, rows[0].length() - 1).split(",").length;
        Matrix newMatrix = new Matrix(nrow, ncolumn);
        for (int i = 0; i < newMatrix.matrixData.length && i < rows.length; i++) {
            //trim leading and trailing square brackets
            String[] columns = rows[i].substring(1, rows[i].length() - 1).split(",");
            for (int j = 0; j < newMatrix.matrixData[i].length && j < columns.length; j++) {
                newMatrix.matrixData[i][j] = Double.valueOf(columns[j]);
            }
        } return newMatrix;
    }
    public Matrix transpose() {
        Matrix transposedMatrix = new Matrix(getNumColumns(), getNumRows()); for (int i = 0; i < getNumRows(); i++) {
            for (int j = 0; j < getNumColumns(); j++) {
                transposedMatrix.set(j, i, get(i, j));
            }
        } return transposedMatrix;
    }
    public boolean isSquare() {return rows == columns;}
    public Matrix createSubMatrix(int excluding_row, int excluding_col) {
        Matrix mat = new Matrix(getNumRows() - 1, getNumColumns() - 1); int r = -1;
        for (int i = 0; i < getNumRows(); i++) {
            if (i == excluding_row) continue; r++; int c = -1; for (int j = 0; j < getNumColumns(); j++) {
                if (j == excluding_col) continue; mat.set(r, ++c, get(i, j));
            }
        } return mat;
    }
    public int size() {return rows * columns;}
    private int changeSign(int val) {if (val % 2 == 0) {return 1;} return -1;}
    public double determinant() {return determinant(this);}
    public double determinant(Matrix matrix) {
        if (!matrix.isSquare()) throw new IllegalArgumentException("Matrix needs to be square.");
        if (matrix.size() == 1) {
            return matrix.get(0, 0);
        } if (matrix.size() == 2) {
            return (matrix.get(0, 0) * matrix.get(1, 1)) - (matrix.get(0, 1) * matrix.get(1, 0));
        } double sum = 0.0; for (int i = 0; i < matrix.getNumColumns(); i++) {
            sum += changeSign(i) * get(0, i) * determinant(createSubMatrix(0, i));
        } return sum;
    }
    public Matrix inverse() {
        return MatrixOperations.multiply(cofactor().transpose(), 1.0 / determinant());
    }
    public Matrix cofactor() {
        Matrix mat = new Matrix(getNumRows(), getNumColumns()); for (int i = 0; i < getNumRows(); i++) {
            for (int j = 0; j < getNumColumns(); j++) {
                mat.set(i, j, changeSign(i) * changeSign(j) * determinant(createSubMatrix(i, j)));
            }
        } return mat;
    }
    @Override
    public synchronized boolean equals(Object that) {
        if (that == null) {return false;} if (!(that instanceof Matrix)) {return false;} Matrix other = (Matrix) that;
        if (!(getNumColumns() == other.getNumColumns() && getNumRows() == other.getNumRows())) return false;
        for (int i = 0; i < other.getNumRows(); i++) {
            for (int j = 0; j < other.getNumColumns(); j++) {
                if (matrixData[i][j] != other.getMatrixData()[i][j]) return false;
            }
        } return true;
    }
    @Override
    public String toString() {
        String matrix = ""; for (double[] aMatrixData : matrixData) {
            for (double anAMatrixData : aMatrixData) {matrix += anAMatrixData + ",";}
            matrix = "[" + matrix.substring(0, matrix.length() - 1) + "];";
        }//remove trailing ','}
        return "[" + matrix.substring(0, matrix.length() - 1) + "]";//remove trailing ';'
    }
    public synchronized double get(int i, int j) {
        return matrixData[i][j];
    }
    @Override
    public int compareTo(Matrix other) {
        if (other == null) {return 0;}
        if (other.getNumColumns() + other.getNumRows() != getNumColumns() + getNumRows()) {
            return (other.getNumColumns() + other.getNumRows()) - (getNumColumns() + getNumRows());
        } else {return (int) (other.sumAllElements() - sumAllElements());}
    }
    private double sumAllElements() {
        double sum = 0; for (double[] row : matrixData) {for (double element : row) {sum += element;}} return sum;
    }
}