package in.tamchow.fractal.config.fractalconfig.l_system;
import in.tamchow.fractal.config.Config;
import in.tamchow.fractal.config.Strings;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import java.io.Serializable;
/**
 * Holds Parameters for an LS fractal
 */
public class LSFractalParams extends Config implements Serializable {
    public PixelContainer.PostProcessMode postProcessMode;
    private String axiom;
    private int depth, init_length, fore_color, back_color, fps;
    private double init_angle;
    private UnitGrammar[] grammar;
    {
        setName(Strings.BLOCKS.LS);
    }
    public LSFractalParams(@NotNull LSFractalParams old) {
        super(old.height, old.width, old.path);
        setPostProcessMode(old.getPostProcessMode());
        setInit_length(old.getInit_length());
        setInit_angle(old.getInit_angle());
        setAxiom(old.getAxiom());
        setDepth(old.getDepth());
        setGrammar(old.getGrammar());
    }
    public LSFractalParams() {
        super();
        setPostProcessMode(PixelContainer.PostProcessMode.NONE);
        setFps(0);
    }
    public String getAxiom() {
        return axiom;
    }
    public void setAxiom(String axiom) {
        this.axiom = axiom;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
    public UnitGrammar[] getGrammar() {
        return grammar;
    }
    public void setGrammar(@NotNull UnitGrammar[] grammar) {
        this.grammar = new UnitGrammar[grammar.length];
        for (int i = 0; i < this.grammar.length; i++) {
            this.grammar[i] = new UnitGrammar(grammar[i]);
        }
    }
    public double getInit_angle() {
        return init_angle;
    }
    public void setInit_angle(double init_angle) {
        this.init_angle = init_angle;
    }
    public int getInit_length() {
        return init_length;
    }
    public void setInit_length(int init_length) {
        this.init_length = init_length > width ? width : init_length;
    }
    public PixelContainer.PostProcessMode getPostProcessMode() {
        return postProcessMode;
    }
    public void setPostProcessMode(PixelContainer.PostProcessMode postProcessMode) {
        this.postProcessMode = postProcessMode;
    }
    public void fromString(@NotNull String[] data) {
        super.fromString(data[0].split(Strings.CONFIG_SEPARATOR));
        @NotNull String[] init = StringManipulator.split(data[1], ",");
        if (init.length >= 4) {
            setDepth(Integer.valueOf(init[0]));
            setAxiom(init[1]);
            setFore_color(Integer.valueOf(init[2], 16));
            setBack_color(Integer.valueOf(init[3], 16));
            switch (init.length) {
                case 4:
                    setInit_length(width);
                    setInit_angle(0.0);
                    break;
                case 5:
                    try {
                        setInit_length(Integer.valueOf(init[4]));
                        setInit_angle(0.0);
                    } catch (NumberFormatException nfe) {
                        setInit_angle(Double.valueOf(init[4]));
                        setInit_length(width);
                    }
                    break;
                case 6:
                    setInit_length(Integer.valueOf(init[4]));
                    setInit_angle(Double.valueOf(init[5]));
                    break;
            }
        }
        grammar = new UnitGrammar[data.length - 1];
        for (int i = 1; i < data.length; i++) {
            grammar[i - 1] = new UnitGrammar(data[i]);
        }
    }
    public int getFore_color() {
        return fore_color;
    }
    public void setFore_color(int fore_color) {
        this.fore_color = fore_color;
    }
    public int getBack_color() {
        return back_color;
    }
    public void setBack_color(int back_color) {
        this.back_color = back_color;
    }
    @Override
    public String toString() {
        String representation = super.toString() + "\n" + depth + "," + axiom + "," + fore_color + "," + back_color + "," + init_length + "," + init_angle;
        for (UnitGrammar rule : grammar) {
            representation += "\n" + rule;
        }
        return representation;
    }
}