package in.tamchow.fractal.fractals.l_system;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.fractalconfig.l_system.UnitGrammar;
import in.tamchow.fractal.fractals.FractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.graphics.DrawingUtilities;
import in.tamchow.fractal.graphicsutilities.graphics.Turtle;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.helpers.strings.StringManipulator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * generates L-System Fractals. Does not implement panning or zooming, as those make no sense
 */
public class LSFractalGenerator implements FractalGenerator {
    LSFractalParams params;
    PixelContainer canvas;
    Turtle turtle;
    String[] generations;
    Publisher publisher;
    public LSFractalGenerator(@NotNull LSFractalParams params, Publisher publisher) {
        this.params = params;
        canvas = new PixelContainer(params.getWidth(), params.getHeight());
        DrawingUtilities.fill(canvas, params.getBack_color());
        turtle = new Turtle(canvas, Math.abs(canvas.getWidth() - params.getInit_length()) / 2, canvas.getHeight() / 2, params.getBack_color(), params.getFore_color(), params.getInit_angle());
        generations = new String[params.getDepth()];
        generations[0] = params.getAxiom();
        this.publisher = publisher;
    }
    public void generate() {
        for (int i = 0, k = 1; i < generations.length - 1 && k < generations.length; ++i, ++k) {
            for (int j = 0; j < generations[i].length(); ++j) {
                @NotNull String toEvolve = String.valueOf(generations[i].charAt(j));
                @Nullable UnitGrammar evolutions = getGrammarForCode(toEvolve);
                if (evolutions == null) {
                    throw new LSGrammarException("Undefined code encountered.");
                } else {
                    UnitGrammar.TransformRule evolution;
                    if (evolutions.transformRules.length == 1 && evolutions.transformRules[0].isDeterministic()) {
                        evolution = evolutions.transformRules[0];
                    } else if (evolutions.transformRules.length == 1 && (!evolutions.transformRules[0].isDeterministic())) {
                        throw new LSGrammarException("Malformed Transformation Rule.");
                    } else {
                        evolution = evolutions.transformRules[MathUtils.boundsProtected(MathUtils.weightedRandom(evolutions.getWeights()), evolutions.transformRules.length)];
                    }
                    generations[k] = StringManipulator.replace(generations[i], toEvolve, evolution.transformTo);
                }
                publishprogress(i + 1);
            }
        }
    }
    private void publishprogress(int progress) {
        float completion = ((float) (progress)) / generations.length;
        publisher.publish("Completion = " + (completion * 100.0f) + " %", completion);
    }
    private UnitGrammar getGrammarForCode(String code) {
        for (@NotNull UnitGrammar grammar : params.getGrammar()) {
            if (grammar.code.equals(code)) {
                return grammar;
            }
        }
        return null;
    }
    public void drawState(int index) {
        drawState(getStateAtIndex(index));
    }
    public String getStateAtIndex(int index) {
        return generations[MathUtils.boundsProtected(index, generations.length)];
    }
    public void drawState(@NotNull String stateToDraw) {
        double segmentlength = params.getInit_length() * Math.cos(params.getInit_angle()) / stateToDraw.length();
        for (int i = 0; i < stateToDraw.length(); ++i) {
            @Nullable UnitGrammar grammar = getGrammarForCode(stateToDraw.charAt(i) + "");
            if (grammar == null) {
                throw new LSGrammarException("Undefined code encountered.");
            }
            double data = 0;
            switch (grammar.command) {
                case DRAW_FORWARD:
                    data = segmentlength;
                    break;
                case TURN_LEFT:
                case TURN_RIGHT:
                    data = grammar.angle;
                    break;
            }
            turtle.draw(grammar.command, data);
        }
    }
    @NotNull
    public Animation drawStatesAsAnimation() {
        if (params.getFps() <= 0) {
            throw new UnsupportedOperationException("FPS of 0 or below is illegal.");
        }
        @NotNull Animation frames = new Animation(params.getFps());
        for (@NotNull String state : generations) {
            drawState(state);
            frames.addFrame(getCanvas());
        }
        return frames;
    }
    public PixelContainer getCanvas() {
        return canvas;
    }
    public LSFractalParams getParams() {
        return params;
    }
}