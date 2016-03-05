package in.tamchow.fractal.config;
/**
 * Defines a progress publisher's method contract.
 */
public interface Publisher {
    void publish(String message, double progress);
    void publish(String message, double progress, Object... args);
}