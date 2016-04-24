package in.tamchow.fractal.math;
import in.tamchow.fractal.helpers.annotations.Nullable;
/**
 * Holds possible types of comparisons
 */
public enum Comparator {
    LESS_THAN("<"), GREATER_THAN(">"), LESS_THAN_OR_EQUAL_TO("<="), GREATER_THAN_OR_EQUAL_TO(">="), EQUAL_TO("="), NOT_EQUAL_TO("!=");
    private String alias;
    Comparator(String alias) {
        this.alias = alias;
    }
    @Nullable
    public static Comparator fromAlias(String alias) {
        switch (alias) {
            case "<":
                return LESS_THAN;
            case ">":
                return GREATER_THAN;
            case "<=":
                return LESS_THAN_OR_EQUAL_TO;
            case ">=":
                return GREATER_THAN_OR_EQUAL_TO;
            case "=":
                return EQUAL_TO;
            case "!=":
                return NOT_EQUAL_TO;
            default:
                return null;
        }
    }
    public String getAlias() {
        return alias;
    }
}