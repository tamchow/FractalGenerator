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

    private void initMatrix(int rows, int columns, double[][] matrixData) {
        setMatrixData(matrixData);
        setNumRows(rows);
        setNumColumns(columns);
    }

    public double get(int i, int j) {
        return matrixData[i][j];
    }

    public void set(int i, int j, double value) {
        matrixData[i][j] = value;
    }
}
