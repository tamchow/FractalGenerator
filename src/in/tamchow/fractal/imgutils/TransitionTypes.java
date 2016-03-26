package in.tamchow.fractal.imgutils;

/**
 * Has codes for transition types.
 * Types:
 * 1)from left
 * 2)from right
 * 3)from top
 * 4)from bottom
 * 5)from edges towards centre
 * 6)from centre towards edges
 * 7)Crossfade
 */
public enum TransitionTypes {
    TOP, BOTTOM, LEFT, RIGHT, EDGE_IN, CENTRE_OUT, CROSSFADE, NONE
}