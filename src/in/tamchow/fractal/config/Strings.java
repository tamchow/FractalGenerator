package in.tamchow.fractal.config;
/**
 * Holds commonly-used strings in configuration files
 */
public class Strings {
    public static final String COMMENT = "#";
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
        public static final String GLOBALS = "[Globals]", FRACTALS = "[Fractals]", IMAGE = "[ImageConfig]", COMPLEX = "[ComplexFractalConfig]",
                INIT = "[InitConfig]", RUN = "[RunConfig]", COMPLEXBROT = "[ComplexBrotFractalConfig]", IFS = "[IFSFractalConfig]",
                LS = "[LSFractalConfig]", ZOOMS = "[Zooms]",
                ENDGLOBALS = "[EndGlobals]", ENDFRACTALS = "[EndFractals]", ENDIMAGE = "[EndImageConfig]", ENDCOMPLEX = "[EndComplexFractalConfig]",
                ENDINIT = "[EndInitConfig]", ENDRUN = "[EndRunConfig]", ENDCOMPLEXBROT = "[EndComplexBrotFractalConfig]",
                ENDIFS = "[EndIFSFractalConfig]", ENDLS = "[EndLSFractalConfig]", ENDZOOMS = "[EndZooms]";
        private BLOCKS() {
        }
    }
}