package in.tamchow.fractal.platform_tools;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.helpers.annotations.NotNull;
/**
 * For JavaSE environments for publishing fractal generation progress
 */
public class DesktopProgressPublisher implements Publisher {
    @Override
    public synchronized void publish(String message, double progress, int data) {
        System.out.println(message);
    }
    @Override
    public void publish(@NotNull String message, double progress, int data, Object... args) {
        System.out.format(message, args);
    }
}