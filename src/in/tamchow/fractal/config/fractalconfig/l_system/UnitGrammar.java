package in.tamchow.fractal.config.fractalconfig.l_system;
import in.tamchow.fractal.helpers.StringManipulator;
import in.tamchow.fractal.imgutils.graphics.Turtle;

import java.io.Serializable;
/**
 * Holds data about 1 unit of grammar in the L-system
 */
public class UnitGrammar implements Serializable {
    private static final double ERR = 1E-14;
    public String code;
    public Turtle.TurtleCommand command;
    public double angle;
    public TransformRule[] transformRules;
    public UnitGrammar(UnitGrammar old) {
        this(old.code, old.command, old.angle, old.transformRules);
    }
    public UnitGrammar(String code, Turtle.TurtleCommand command, double angle, TransformRule[] transformRules) {
        this.code = code;
        this.command = command;
        this.angle = angle;
        this.transformRules = new TransformRule[transformRules.length];
        for (int i = 0; i < this.transformRules.length; i++) {
            this.transformRules[i] = new TransformRule(transformRules[i]);
        }
    }
    public UnitGrammar(String data) {
        String[] parts = StringManipulator.split(data, "=");
        String[] once = StringManipulator.split(parts[0], ",");
        if (parts.length == 1) {
            transformRules = new TransformRule[]{new TransformRule()};
        } else {
            String[] more = StringManipulator.split(parts[1], ",");
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
            command = Turtle.TurtleCommand.valueOf(once[1]);
            angle = 0.0;
        } else {
            code = once[0];
            command = Turtle.TurtleCommand.valueOf(once[1]);
            angle = Double.valueOf(once[2]);
        }
    }
    public double[] getWeights() {
        double[] weights = new double[transformRules.length];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = transformRules[i].probability;
        }
        return weights;
    }
    @Override
    public String toString() {
        String representation = code + "," + command + "," + angle + "=";
        for (TransformRule rule : transformRules) {
            representation += rule + ",";
        }
        return representation.substring(0, representation.length() - 1);
    }
    public class TransformRule {
        public String transformTo;
        public double probability;
        public TransformRule() {
            this(code, 1.0);
        }
        public TransformRule(String transformTo, double probability) {
            this.transformTo = transformTo;
            this.probability = probability;
        }
        public TransformRule(TransformRule old) {
            this(old.transformTo, old.probability);
        }
        public TransformRule(String rule) {
            String[] parts = StringManipulator.split(rule, ":");
            if (parts.length == 1 && parts[0].length() == 0) {
                transformTo = code;
                probability = 1;
            } else if (parts.length == 1 && parts[0].length() > 0) {
                transformTo = parts[0];
                probability = 1;
            } else {
                transformTo = parts[0];
                probability = Double.valueOf(parts[1]);
            }
        }
        public boolean isDeterministic() {
            return probability > (1.0 - ERR) && probability < (1.0 + ERR);
        }
        @Override
        public String toString() {
            return transformTo + ":" + probability;
        }
    }
}