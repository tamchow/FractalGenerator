package in.tamchow.fractal.math.matrix;
/**
 * Holds a rectangular matrix
 */
public class Matrix {
    private int rows, columns;
    private double[][] matrixData;
    public Matrix(double[][] matrixData) {
        initMatrix(matrixData.length, matrixData[0].length, matrixData);
    }
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
            System.arraycopy(matrixData[i], 0, this.matrixData[i], 0, matrixData.length);
        }
    }
    public Matrix(int rows, int columns) {
        setNumRows(rows); setNumColumns(columns); matrixData = new double[this.rows][this.columns];
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
    public synchronized boolean equals(Object that) {
        if (!(that instanceof Matrix)) {return false;} Matrix other = (Matrix) that;
        if (!(getNumColumns() == other.getNumColumns() && getNumRows() == other.getNumRows())) return false;
        for (int i = 0; i < other.getNumRows(); i++) {
            for (int j = 0; j < other.getNumColumns(); j++) {
                if (matrixData[i][j] != other.getMatrixData()[i][j]) return false;
            }
        } return true;
    }
    public String toString() {
        String matrix = ""; for (double[] aMatrixData : matrixData) {
            for (int j = 0; j < aMatrixData.length; j++) {matrix += aMatrixData[j] + ",";}
            matrix = "[" + matrix.substring(0, matrix.length() - 1) + "];";
        }//remove trailing ','}
        return "[" + matrix.substring(0, matrix.length() - 1) + "]";//remove trailing ';'
    }
    public synchronized double get(int i, int j) {
        return matrixData[i][j];
    }
    public synchronized void set(int i, int j, double value) {
        matrixData[i][j] = value;
    }
}
