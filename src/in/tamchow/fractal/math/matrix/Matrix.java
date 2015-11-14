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

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
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
        setRows(rows);
        setColumns(columns);
    }

    public double get(int i, int j) {
        return matrixData[i][j];
    }

    public void set(int i, int j, double value) {
        matrixData[i][j] = value;
    }
}
