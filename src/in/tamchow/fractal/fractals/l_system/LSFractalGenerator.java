package in.tamchow.fractal.fractals.l_system;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.fractalconfig.l_system.UnitGrammar;
import in.tamchow.fractal.helpers.MathUtils;
import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.imgutils.containers.Animation;
import in.tamchow.fractal.imgutils.containers.ImageData;
import in.tamchow.fractal.imgutils.graphics.Turtle;
/**
 * generates L-System Fractals. Does not implement panning or zooming, as those make no sense
 */
public class LSFractalGenerator {
    LSFractalParams params;
    ImageData canvas;
    Turtle turtle;
    String[] generations;
    Publisher publisher;
    public LSFractalGenerator(LSFractalParams params, Publisher publisher) {
        this.params = params;
        canvas = new ImageData(params.getWidth(), params.getHeight());
        canvas.fill(params.getBack_color());
        turtle = new Turtle(canvas, Math.abs(canvas.getWidth() - params.getInit_length()) / 2, canvas.getHeight() / 2, params.getBack_color(), params.getFore_color(), params.getInit_angle());
        generations = new String[params.getDepth()];
        generations[0] = params.getAxiom();
        this.publisher = publisher;
    }
    public void generate() {
        for (int i = 0, k = 1; i < generations.length - 1 && k < generations.length; ++i, ++k) {
            for (int j = 0; j < generations[i].length(); ++j) {
                String toEvolve = generations[i].charAt(j) + "";
                UnitGrammar evolutions = getGrammarForCode(toEvolve);
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
        float completion = ((float) (progress)) * 100.0f / generations.length;
        publisher.publish("Completion = " + completion + " %", completion);
    }
    private UnitGrammar getGrammarForCode(String code) {
        for (UnitGrammar grammar : params.getGrammar()) {
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
    public void drawState(String stateToDraw) {
        double segmentlength = params.getInit_length() * Math.cos(params.getInit_angle()) / stateToDraw.length();
        for (int i = 0; i < stateToDraw.length(); ++i) {
            UnitGrammar grammar = getGrammarForCode(stateToDraw.charAt(i) + "");
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
    public Animation drawStatesAsAnimation() {
        if (params.getFps() <= 0) {
            throw new UnsupportedOperationException("FPS of 0 or below is illegal.");
        }
        Animation frames = new Animation(params.getFps());
        for (String state : generations) {
            drawState(state);
            frames.addFrame(getCanvas());
        }
        return frames;
    }
    public ImageData getCanvas() {
        return canvas;
    }
    public LSFractalParams getParams() {
        return params;
    }
}