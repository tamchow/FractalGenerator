package in.tamchow.fractal.config;
/**
 * Holds commonly-used strings in configuration files
 */
public class Strings {
    public static final String COMMENT = "#", CONFIG_SEPARATOR = " ";
    private Strings() {
    }
    public static class DECLARATIONS {
        public static final String SWITCH_RATE = "Switch_Mode_Rate:", NEWTON_CONSTANT = "Newton_constant:", POSTPROCESSING = "Postprocessing:",
                DIMENSIONS = "Dimensions:", OLD_VARIABLE_CODE = "Old_variable_code:", THREADS = "Threads:", FRAMESKIP = "Frameskip:",
                TRAP_POINT = "Trap_point:", TRAP_LINE = "Trap_line:";
        private DECLARATIONS() {
        }
    }
    public static class BLOCKS {
        public static final String IMAGE = "[ImageConfig]", COMPLEX = "[ComplexFractalConfig]",
                INIT = "[InitConfig]", RUN = "[RunConfig]", COMPLEXBROT = "[ComplexBrotFractalConfig]", IFS = "[IFSFractalConfig]",
                LS = "[LSFractalConfig]", ZOOMS = "[Zooms]", ENDINIT = "[EndInitConfig]", ENDRUN = "[EndRunConfig]", ENDZOOMS = "[EndZooms]";
        private BLOCKS() {
        }
    }
}