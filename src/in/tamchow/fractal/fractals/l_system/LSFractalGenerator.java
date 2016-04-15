package in.tamchow.fractal.fractals.l_system;
import in.tamchow.fractal.config.Publisher;
import in.tamchow.fractal.config.fractalconfig.l_system.LSFractalParams;
import in.tamchow.fractal.config.fractalconfig.l_system.UnitGrammar;
import in.tamchow.fractal.fractals.FractalGenerator;
import in.tamchow.fractal.graphicsutilities.containers.Animation;
import in.tamchow.fractal.graphicsutilities.containers.LinearizedPixelContainer;
import in.tamchow.fractal.graphicsutilities.containers.PixelContainer;
import in.tamchow.fractal.graphicsutilities.graphics.Turtle;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static in.tamchow.fractal.graphicsutilities.graphics.DrawingUtilities.fill;
import static in.tamchow.fractal.helpers.math.MathUtils.boundsProtected;
import static in.tamchow.fractal.helpers.math.MathUtils.weightedRandom;
/**
 * Generates L-System fractals.
 * Does not implement panning or zooming, as those make no sense for L-System fractals.
 */
public class LSFractalGenerator implements FractalGenerator {
    private static final int BUFFER_MULTIPLIER = 2;
    LSFractalParams params;
    PixelContainer canvas;
    Turtle turtle;
    String[] generations;
    Publisher publisher;
    public LSFractalGenerator(@NotNull LSFractalParams params, Publisher publisher) {
        this.params = params;
        canvas = new LinearizedPixelContainer(params.getWidth(), params.getHeight());
        fill(canvas, params.getBack_color());
        turtle = new Turtle(canvas, Math.abs(canvas.getWidth() - params.getInit_length()) / 2, canvas.getHeight() / 2, params.getBack_color(), params.getFore_color(), params.getInit_angle());
        generations = new String[params.getDepth()];
        generations[0] = params.getAxiom();
        this.publisher = publisher;
    }
    public void generate() {
        for (int i = 0, k = 1; i < generations.length - 1 && k < generations.length; ++i, ++k) {
            @NotNull StringBuilder builder = new StringBuilder(generations[i].length() * BUFFER_MULTIPLIER);
            for (int j = 0; j < generations[i].length(); ++j) {
                @NotNull
                String leftSymbol = String.valueOf(generations[i].charAt(boundsProtected(j - 1, generations[i].length()))),
                        rightSymbol = String.valueOf(generations[i].charAt(boundsProtected(j + 1, generations[i].length()))),
                        toEvolve = String.valueOf(generations[i].charAt(j));
                @Nullable UnitGrammar evolutions = getGrammarForCode(toEvolve);
                if (evolutions == null) {
                    throw new LSGrammarException("Undefined code encountered.");
                } else {
                    if (evolutions.isContextSensitive()) {
                        //Use context-sensitive behaviour as first precedence
                        @Nullable UnitGrammar.TransformRule evolution = getApplicableContextTransform(evolutions, leftSymbol, rightSymbol, false);
                        if (evolution == null) {
                            //context-sensitive transforms inapplicable, try context-free transforms
                            evolution = getApplicableContextTransform(evolutions, leftSymbol, rightSymbol, true);
                            if (evolution == null) {
                                //No production rule found for any - assume identity production
                                builder.append(evolutions.code);
                            } else {
                                //Context-free production rule found - use associated transformation
                                builder.append(evolution.transformTo);
                            }
                        } else {
                            //Production rule found for given context - use associated transformation
                            builder.append(evolution.transformTo);
                        }
                    } else {
                        //Use context-free behaviour
                        builder.append(getDefaultTransformation(evolutions));
                    }
                    /*UnitGrammar.TransformRule evolution = evolutions.transformRules[getTransformRuleIndex(evolutions)];
                    if (evolution.isContextSensitive()) {
                        if ((evolution.left == null || evolution.left.equals(leftSymbol)) &&
                                (evolution.right == null || evolution.right.equals(rightSymbol))) {
                            //Match - use the transformation code
                            builder.append(evolution.transformTo);
                        }else {
                            //Not Match - use identity transformation
                            builder.append(builder.append(evolutions.code));
                        }
                    } else {
                        builder.append(evolution.transformTo);
                    }
                    generations[k] = StringManipulator.replace(generations[i], toEvolve, evolution.transformTo);*/
                }
                generations[k] = builder.toString();
                publishprogress(i + 1);
            }
        }
    }
    private String getDefaultTransformation(@NotNull UnitGrammar evolutions) {
        return evolutions.transformRules[getTransformRuleIndex(evolutions.transformRules, evolutions)].transformTo;
    }
    private UnitGrammar.TransformRule getApplicableContextTransform(@NotNull UnitGrammar evolutions, String left, String right, boolean contextFree) {
        if (!evolutions.isContextSensitive()) {
            return null;
        }
        List<UnitGrammar.TransformRule> rules = new ArrayList<>(evolutions.transformRules.length);
        for (@NotNull UnitGrammar.TransformRule evolution :
                ((contextFree) ? evolutions.getContextFreeTransforms() : evolutions.getContextSensitiveTransforms())) {
            if ((evolution.left == null || evolution.left.equals(left)) &&
                    (evolution.right == null || evolution.right.equals(right))) {
                rules.add(evolution);
            }
        }
        if (rules.size() > 0) {
            return evolutions.transformRules[getTransformRuleIndex(
                    rules.toArray(new UnitGrammar.TransformRule[rules.size()]), evolutions)];
        }
        return null;
    }
    private int getTransformRuleIndex(@NotNull UnitGrammar.TransformRule[] evolutions, @NotNull UnitGrammar grammar) {
        int index;
        if (evolutions.length == 0) {
            throw new LSGrammarException("Malformed Transformation Rule.");
        } else if (evolutions.length == 1 && evolutions[0].isDeterministic()) {
            index = 0;
        } else if (evolutions.length == 1 && (!evolutions[0].isDeterministic())) {
            index = 0;
            //throw new LSGrammarException("Malformed Transformation Rule.");
        } else {
            index = boundsProtected(weightedRandom(grammar.getWeights()), evolutions.length);
        }
        return index;
    }
    private void publishprogress(int progress) {
        float completion = ((float) (progress)) / generations.length;
        publisher.publish("Completion = " + (completion * 100.0f) + " %", completion, progress);
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
        return generations[boundsProtected(index, generations.length)];
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