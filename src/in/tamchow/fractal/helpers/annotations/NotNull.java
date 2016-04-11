package in.tamchow.fractal.helpers.annotations;
import java.lang.annotation.*;
/**
 * Indicates a variable cannot be null
 */
@Documented
@Target({ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER,
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface NotNull {
    boolean enforce() default false;
}