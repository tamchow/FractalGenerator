package in.tamchow.fractal.imgutils.graphics;
import in.tamchow.fractal.imgutils.ImageData;
/**
 * Implements turtle graphics at a very basic level
 *
 * @see in.tamchow.fractal.imgutils.ImageData#drawLine(int, int, int, int, int)
 */
public class Turtle {
    ImageData canvas;
    int x, y;
    int back_color, fore_color;
    double angle;
    public Turtle(ImageData canvas, int x, int y) {
        this(canvas, x, y, 0);
    }
    public Turtle(ImageData canvas, int x, int y, double angle) {
        this(canvas, x, y, 0xffffff, 0x00000, angle);
    }
    public Turtle(ImageData canvas, int x, int y, int back_color, int fore_color, double angle) {
        this.x = x;
        this.y = y;
        this.back_color = back_color;
        this.fore_color = fore_color;
        this.angle = angle;
    }
    public void draw(TurtleCommand command, double data) {
        switch (command) {
            case FILL_CANVAS:
            case CLEAR:
                canvas.fill(back_color);
            case DRAW_FORWARD:
                draw_forward(data);
                break;
            case TURN_LEFT:
                turn(data);
                break;
            case TURN_RIGHT:
                turn(-data);
                break;
            case NO_OP:
            default:
                break;
        }
    }
    private void draw_forward(double step) {
        int oldx = x, oldy = y;
        x += step * Math.cos(Math.toRadians(angle));
        y += step * Math.sin(Math.toRadians(angle));
        canvas.drawLine(oldx, oldy, x, y, fore_color);
    }
    private void turn(double delta) {
        angle += delta;
    }
    public ImageData getCanvas() {
        return canvas;
    }
    public void setCanvas(ImageData canvas) {
        this.canvas = canvas;
    }
    public int getBack_color() {
        return back_color;
    }
    public void setBack_color(int back_color) {
        this.back_color = back_color;
    }
    public int getFore_color() {
        return fore_color;
    }
    public void setFore_color(int fore_color) {
        this.fore_color = fore_color;
    }
    public enum TurtleCommand {
        DRAW_FORWARD, TURN_LEFT, TURN_RIGHT, FILL_CANVAS, CLEAR, NO_OP
    }
}