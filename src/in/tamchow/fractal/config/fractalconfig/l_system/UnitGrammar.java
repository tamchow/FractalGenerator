package in.tamchow.fractal.config.fractalconfig.l_system;
import in.tamchow.fractal.graphicsutilities.graphics.Turtle;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.strings.StringManipulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Holds data about 1 unit of grammar in the L-system
 */
public class UnitGrammar implements Serializable {
    private static final double ERR = 1E-14;
    public String code;
    public Turtle.TurtleCommand command;
    public double angle;
    public TransformRule[] transformRules;
    public UnitGrammar(@NotNull UnitGrammar old) {
        this(old.code, old.command, old.angle, old.transformRules);
    }
    public UnitGrammar(String code, Turtle.TurtleCommand command, double angle, @NotNull TransformRule[] transformRules) {
        this.code = code;
        this.command = command;
        this.angle = angle;
        this.transformRules = new TransformRule[transformRules.length];
        for (int i = 0; i < this.transformRules.length; i++) {
            this.transformRules[i] = new TransformRule(transformRules[i]);
        }
    }
    public UnitGrammar(@NotNull String data) {
        /**
         * Format examples:
         *
         * Codes:
         * Variant 1: A
         * (Maps to {@link in.tamchow.fractal.graphicsutilities.graphics.Turtle.TurtleCommand#NO_OP},
         * angle=0 rad,probability=1.0,transforms to A)
         *
         * Variant 2: A,DRAW_FORWARD
         * (Maps to {@link in.tamchow.fractal.graphicsutilities.graphics.Turtle.TurtleCommand#DRAW_FORWARD},
         * angle=0 rad,probability=1.0 transforms to A)
         *
         * Variant 2: A,3.14
         * (Maps to {@link in.tamchow.fractal.graphicsutilities.graphics.Turtle.TurtleCommand#NO_OP},
         * angle=3.14 rad,probability=1.0 transforms to A)
         *
         *  Variant 3: A,DRAW_FORWARD,3.14
         * (Maps to {@link in.tamchow.fractal.graphicsutilities.graphics.Turtle.TurtleCommand#DRAW_FORWARD},
         * angle=3.14 rad,probability=1.0 transforms to A)
         *
         * Variant 5: A=F or A=F:1.0
         * (or variant 2 or 3 or 4 to the left of '=')
         * (Maps to {@link in.tamchow.fractal.graphicsutilities.graphics.Turtle.TurtleCommand#DRAW_FORWARD},
         * angle=0 rad,probability=1.0 transforms to F)
         *
         * Variant 6: A=F:0.5,C:0.5 (... in similar format)
         * (or variant 2 or 3 or 4 to the left of '=')
         * (Maps to {@link in.tamchow.fractal.graphicsutilities.graphics.Turtle.TurtleCommand#DRAW_FORWARD},
         * angle=0 rad, probability=.5 transforms to F, probability =.5 transforms to C)
         *
         * Any other format may either throw exceptions at initialization or runtime or cause undefined behaviour.
         */
        @NotNull String[] parts = StringManipulator.split(data, "=");
        @NotNull String[] once = StringManipulator.split(parts[0], ",");
        if (parts.length == 1) {
            transformRules = new TransformRule[]{new TransformRule()};
        } else {
            @NotNull String[] more = StringManipulator.split(parts[1], ",");
            transformRules = new TransformRule[more.length];
            for (int i = 0; i < transformRules.length; i++) {
                transformRules[i] = new TransformRule(more[i]);
            }
        }
        if (once.length == 1) {
            code = once[0];
            command = Turtle.TurtleCommand.NO_OP;
            angle = 0.0;
        } else if (once.length == 2) {
            code = once[0];
            try {
                command = Turtle.TurtleCommand.NO_OP;
                angle = Double.valueOf(once[1]);
            } catch (NumberFormatException numberFormatException) {
                command = Turtle.TurtleCommand.valueOf(once[1]);
                angle = 0.0;
            }
        } else {
            code = once[0];
            command = Turtle.TurtleCommand.valueOf(once[1]);
            angle = Double.valueOf(once[2]);
        }
    }
    public boolean isContextSensitive() {
        for (@NotNull TransformRule transformRule : transformRules) {
            if (transformRule.isContextSensitive()) {
                return true;
            }
        }
        return false;
    }
    @NotNull
    public double[] getWeights() {
        @NotNull double[] weights = new double[transformRules.length];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = transformRules[i].probability;
        }
        return weights;
    }
    @NotNull
    @Override
    public String toString() {
        @NotNull String representation = code + "," + command + "," + angle + "=";
        for (TransformRule rule : transformRules) {
            representation += rule + ",";
        }
        return representation.substring(0, representation.length() - 1);//trim trailing ','
    }
    public TransformRule[] getContextSensitiveTransforms() {
        List<TransformRule> rules = new ArrayList<>(transformRules.length);
        for (TransformRule transformRule : transformRules) {
            if (transformRule.isContextSensitive()) {
                rules.add(transformRule);
            }
        }
        return rules.toArray(new TransformRule[rules.size()]);
    }
    public TransformRule[] getContextFreeTransforms() {
        List<TransformRule> rules = new ArrayList<>(transformRules.length);
        for (TransformRule transformRule : transformRules) {
            if (!transformRule.isContextSensitive()) {
                rules.add(transformRule);
            }
        }
        return rules.toArray(new TransformRule[rules.size()]);
    }
    public class TransformRule {
        @Nullable
        public String transformTo, left, right;
        public double probability;//guaranteed to be positive
        public TransformRule() {
            this(code, 1.0);
        }
        public TransformRule(String transformTo, double probability) {
            this.transformTo = transformTo;
            this.probability = Math.abs(probability);
            left = null;
            right = null;
        }
        public TransformRule(@NotNull TransformRule old) {
            this(old.transformTo, old.probability);
        }
        public TransformRule(@NotNull String rule) {
            this();//default initialization in case of empty argument
            @NotNull String[] parts = StringManipulator.split(rule, ":");
            if (!(rule.isEmpty() && parts[0].isEmpty())) {
                switch (parts.length) {
                    case 1:
                        try {
                            transformTo = code;
                            probability = Math.abs(Double.valueOf(parts[0]));
                        } catch (NumberFormatException numberFormatException) {
                            transformTo = parts[0];
                            probability = 1.0;
                        }
                        break;
                    case 2:
                        if (rule.contains("<>")) {
                            transformTo = parts[0];
                            probability = 1.0;
                            @NotNull String[] context = StringManipulator.split(parts[2], "<>");
                            left = context[0].equals("?") ? null : context[0];
                            right = context[1].equals("?") ? null : context[1];
                        } else {
                            transformTo = parts[0];
                            probability = Math.abs(Double.valueOf(parts[1]));
                        }
                        break;
                    case 3:
                        transformTo = parts[0];
                        probability = Math.abs(Double.valueOf(parts[1]));
                        @NotNull String[] context = StringManipulator.split(parts[2], "<>");
                        left = context[0].equals("?") ? null : context[0];
                        right = context[1].equals("?") ? null : context[1];
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal format for transformation rule: " + rule);
                }
            }
        }
        public boolean isContextSensitive() {
            return left != null || right != null;
        }
        public boolean isDeterministic() {
            return probability > (1.0 - ERR) && probability < (1.0 + ERR);
        }
        @NotNull
        @Override
        public String toString() {
            String representation = "";
            if (isContextSensitive()) {
                representation += ((left == null) ? "?" : left) + "<>" + ((right == null) ? "?" : right) + ":";
            }
            representation += transformTo + ":" + probability;
            return representation;
        }
    }
}