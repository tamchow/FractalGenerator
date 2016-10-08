package in.tamchow.fractal.graphics.painting;
import in.tamchow.fractal.graphics.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * Implements turtle painting at a very basic level
 *
 * @see Turtle
 */
public final class Turtle {
    private PixelContainer canvas;
    private double x, y, angle;
    private int back_color, fore_color;
    private double previous_x, previous_y, previous_angle;
    public Turtle(PixelContainer canvas, int x, int y) {
        this(canvas, x, y, 0);
    }
    public Turtle(PixelContainer canvas, int x, int y, double angle) {
        this(canvas, x, y, 0xffffffff, 0xff00000, angle);
    }
    public Turtle(PixelContainer canvas, int x, int y, int back_color, int fore_color, double angle) {
        this.canvas = canvas;
        this.x = x;
        this.y = y;
        this.back_color = back_color;
        this.fore_color = fore_color;
        this.angle = angle;
        previous_x = this.x;
        previous_y = this.y;
        previous_angle = this.angle;
    }
    public void draw(@NotNull TurtleCommand command, double data) {
        switch (command) {
            case SAVE:
                previous_angle = angle;
                previous_x = x;
                previous_y = y;
                break;
            case RELOAD:
                angle = previous_angle;
                x = previous_x;
                y = previous_y;
                break;
            case FILL_CANVAS:
                DrawingUtilities.fill(canvas, fore_color);
                break;
            case CLEAR:
                DrawingUtilities.fill(canvas, back_color);
                break;
            case MOVE_FORWARD:
                draw_forward(back_color, data);
                break;
            case DRAW_FORWARD:
                draw_forward(fore_color, data);
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
    private void draw_forward(int color, double step) {
        double oldx = x, oldy = y;
        x += step * Math.cos(Math.toRadians(angle));
        y += step * Math.sin(Math.toRadians(angle));
        DrawingUtilities.drawLine(canvas, oldx, oldy, x, y, color);
    }
    private void turn(double delta) {
        angle += delta;
    }
    public PixelContainer getCanvas() {
        return canvas;
    }
    public void setCanvas(PixelContainer canvas) {
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
        DRAW_FORWARD, MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, FILL_CANVAS, CLEAR, SAVE, RELOAD, NO_OP
    }
}