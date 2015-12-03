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

    public void fromString(String matrix) {
        matrix = matrix.substring(1, matrix.length() - 1);//trim leading and trailing square brackets
        String[] rows = matrix.split(";");
        this.rows = rows.length;
        this.columns = rows[0].substring(1, rows[0].length() - 1).split(",").length;
        matrixData = new double[this.rows][this.columns];
        for (int i = 0; i < matrixData.length && i < rows.length; i++) {
            String[] columns = rows[i].substring(1, rows[i].length() - 1).split(",");
            for (int j = 0; j < matrixData[i].length && j < columns.length; j++) {
                matrixData[i][j] = Double.valueOf(columns[j]);
            }
        }
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
