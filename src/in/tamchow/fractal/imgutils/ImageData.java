package in.tamchow.fractal.imgutils;

/**
 * Encapsulates an image or animation frame: here for platform independence, takes int32 packed RGBA values as pixels;
 */
public class ImageData {
    private String path;
    private int[][] pixdata;

    public ImageData() {
        path = "";
        pixdata = new int[1000][1000];
        for (int i = 0; i < pixdata.length; i++) {
            for (int j = 0; j < pixdata[i].length; j++) {
                pixdata[i][j] = 0x00000000;
            }
        }
    }

    public ImageData(int w, int h) {
        path = "";
        pixdata = new int[h][w];
        for (int i = 0; i < pixdata.length; i++) {
            for (int j = 0; j < pixdata[i].length; j++) {
                pixdata[i][j] = 0x00000000;
            }
        }
    }

    public ImageData(int[][] pixdata) {
        path = "";
        setPixdata(pixdata);
    }

    public ImageData(ImageData img) {
        setPixdata(img.getPixdata());
        path = img.getPath();
    }

    public ImageData(String path) {
        this.path = path;
        pixdata = null;
        /*pixdata = new int[1000][1000];
        for (int i = 0; i < pixdata.length; i++) {
            for (int j = 0; j < pixdata[i].length; j++) {
                pixdata[i][j] = 0x00000000;
            }
        }*/
    }

    public ImageData getPostProcessed() {
        ImageData processed = new ImageData(this);
        for (int i = 1; i < processed.getPixdata().length - 1; i++) {
            for (int j = 1; j < processed.getPixdata()[i].length - 1; j++) {
                int left = pixdata[i][j - 1], right = pixdata[i][j + 1], top = pixdata[i - 1][j], bottom = pixdata[i + 1][j];
                int top_left = pixdata[i - 1][j - 1], top_right = pixdata[i - 1][j + 1], bottom_left = pixdata[i + 1][j - 1], bottom_right = pixdata[i + 1][j + 1];
                double average = (top_left + top + top_right + left + right + bottom_left + bottom + bottom_right) / 8;
                processed.setPixel(i, j, (int) ((average + pixdata[i][j]) / 2));
            }
        }
        return processed;
    }

    public ImageData getColorAveraged() {
        ImageData processed = new ImageData(this);
        for (int i = 1; i < processed.getPixdata().length - 1; i++) {
            for (int j = 1; j < processed.getPixdata()[i].length - 1; j++) {
                int left = pixdata[i][j - 1], right = pixdata[i][j + 1], top = pixdata[i - 1][j], bottom = pixdata[i + 1][j];
                int top_left = pixdata[i - 1][j - 1], top_right = pixdata[i - 1][j + 1], bottom_left = pixdata[i + 1][j - 1], bottom_right = pixdata[i + 1][j + 1];
                double average = (top_left + top + top_right + left + right + bottom_left + bottom + bottom_right) / 8;
                processed.setPixel(i, j, (int) average);
            }
        }
        return processed;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSize(int height, int width){
        int[][]tmp=new int[pixdata.length][pixdata[0].length];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, tmp[i], 0, this.pixdata[i].length);
        }
        this.pixdata = new int[height][width];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(tmp[i], 0, this.pixdata[i], 0, this.pixdata[i].length);
        }
    }
    public int getHeight() {
        return pixdata.length;
    }

    public int getWidth() {
        return pixdata[0].length;
    }

    public int[][] getPixdata() {
        return pixdata;
    }

    public void setPixdata(int[][] pixdata) {
        this.pixdata = new int[pixdata.length][pixdata[0].length];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, this.pixdata[i], 0, this.pixdata[i].length);
        }
    }

    public void setPixdata(int[] pixdata, int scan) {
        this.pixdata = new int[pixdata.length / scan][scan];
        for (int i = 0; i < this.pixdata.length; i++) {
            System.arraycopy(pixdata, i * scan, this.pixdata[i], 0, this.pixdata[i].length);
        }
    }

    public int[] getPixels() {
        int[] pixels = new int[pixdata.length * pixdata[0].length];
        for (int i = 0; i < pixdata.length; i++) {
            System.arraycopy(pixdata[i], 0, pixels, i * pixdata[i].length, pixdata[i].length);
        }
        return pixels;
    }

    public int getPixel(int y, int x) {
        return pixdata[y][x];
    }

    public void setPixel(int y, int x, int val) {
        pixdata[y][x] = val;
    }

    public int getPixel(int i) {
        return pixdata[i / pixdata[0].length][i % pixdata[0].length];
    }

    public void setPixel(int i, int val) {
        pixdata[i / pixdata[0].length][i % pixdata[0].length] = val;
    }
}