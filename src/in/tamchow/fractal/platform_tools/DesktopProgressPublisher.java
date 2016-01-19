package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.config.Printable;
/**
 * For JavaSE environments for publishing fractal generation progress
 */
public class DesktopProgressPublisher implements Printable {
    public synchronized void println(String toPrint) {
        System.out.println(toPrint);
    }
}
