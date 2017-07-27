package in.tamchow.fractal.color;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.helpers.math.MathUtils;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;
import java.util.function.Function;

import static in.tamchow.fractal.helpers.math.MathUtils.*;
import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
import static in.tamchow.fractal.math.complex.ComplexOperations.divide;
import static in.tamchow.fractal.math.complex.ComplexOperations.principallog;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
/**
 * Holds colour configuration for  custom palettes
 */
@SuppressWarnings("unused")
public final class ColorData implements Serializable {
    private static int tintCount = 0;
    private static double tints = 0;
    public Colors.MODE mode;
    @Nullable
    public int[] palette;
    private int baseColor, step, colorDensity, numColors, byParts, interpolationKind;
    private Complex smoothingBase;
    private double periodicity, phaseShift, multiplierThreshold = 1.0, scale = 1.0, weight = 1.0;
    private Colors.PALETTE paletteType;
    private InterpolationType interpolationType;
    private boolean logIndex, exponentialSmoothing, cyclic, modifierEnabled, colors_corrected,
            alreadyCyclic, gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode;
    public ColorData(Colors.MODE mode, int colorDensity, int numColors, int baseColor, InterpolationType interpolationType,
                     boolean gammaCorrection, boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode,
                     int interpolationKind) {
        init(mode, colorDensity, numColors, baseColor, 0, false, false, interpolationType,
                gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
    }
    public ColorData(Colors.MODE mode, int colorDensity, int numColors, int baseColor, int byParts, boolean logIndex,
                     boolean cyclic, InterpolationType interpolationType, boolean gammaCorrection,
                     boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode, int interpolationKind) {
        init(mode, colorDensity, numColors, baseColor, byParts, logIndex, cyclic, interpolationType,
                gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
    }
    public ColorData(Colors.MODE mode, int colorDensity, int numColors, int baseColor, int step, int byParts,
                     boolean logIndex, boolean cyclic, InterpolationType interpolationType, boolean gammaCorrection,
                     boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode, int interpolationKind) {
        init(mode, colorDensity, numColors, baseColor, step, byParts, logIndex, cyclic, interpolationType,
                gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
    }
    public ColorData(Colors.MODE mode, @NotNull int[] palette) {
        setPalette(palette, false);
        setMode(mode);
        setByParts(0);
    }
    public ColorData(Colors.MODE mode, int colorDensity, int numColors, int byParts, boolean logIndex, boolean cyclize,
                     InterpolationType interpolationType, boolean gammaCorrection,
                     boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode, int interpolationKind) {
        this(mode, colorDensity, numColors, byParts, logIndex, cyclize, interpolationType, 0.0, gammaCorrection,
                splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
    }
    public ColorData(Colors.MODE mode, int colorDensity, int numColors, int byParts, boolean logIndex, boolean cyclize,
                     InterpolationType interpolationType, double periodicity, boolean gammaCorrection,
                     boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode, int interpolationKind) {
        this(mode, colorDensity, numColors, byParts, logIndex, cyclize, interpolationType, periodicity, 0.0,
                gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
    }
    public ColorData(Colors.MODE mode, int colorDensity, int numColors, int byParts, boolean logIndex, boolean cyclize,
                     InterpolationType interpolationType, double periodicity, double phaseShift, boolean gammaCorrection,
                     boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode, int interpolationKind) {
        init(mode, numColors, byParts, logIndex, cyclize, interpolationType,
                gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
        setColorDensity(colorDensity);
        setPeriodicity(periodicity);
        setPhaseShift(phaseShift);
    }
    public ColorData(Colors.MODE mode, int numColors, int byParts, boolean logIndex, boolean cyclize,
                     InterpolationType interpolationType, boolean gammaCorrection,
                     boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode, int interpolationKind) {
        init(mode, numColors, byParts, logIndex, cyclize, interpolationType,
                gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
        setColorDensity(-1);
    }
    public ColorData() {
        palette = null;
        init(Colors.MODE.SIMPLE, 0, 0x0, 0, 0, false, false,
                InterpolationType.CATMULL_ROM_SPLINE, false, false, false, 0);
    }
    public ColorData(@NotNull ColorData old) {
        init(old.getMode(), old.getColorDensity(), old.getNumColors(), old.getBaseColor(), old.getStep(),
                old.getByParts(), old.isLogIndex(), old.isCyclic(), old.getInterpolationType(), old.isGammaCorrection(),
                old.isSplineInterpolationCalcMode(), old.isSplineInterpolationResultMode(), old.getInterpolationKind());
        setScale(old.getScale());
        setWeight(old.getWeight());
        setPeriodicity(old.getPeriodicity());
        setMultiplierThreshold(old.getMultiplierThreshold());
        setPhaseShift(old.getPhaseShift());
        setPaletteType(old.getPaletteType());
        setModifierEnabled(old.isModifierEnabled());
        setExponentialSmoothing(old.isExponentialSmoothing());
        setPalette(old.getPalette(), false);
        colors_corrected = old.colors_corrected;
    }
    public static int packRGB(int r, int g, int b) {
        return packARGB(0xff, r, g, b);
    }
    public static int packARGB(int a, int r, int g, int b) {
        return a << (Colors.RGBCOMPONENTS.ALPHA << 3) | r << (Colors.RGBCOMPONENTS.RED << 3) | g << (Colors.RGBCOMPONENTS.GREEN << 3) | b;
        //int argb=a;argb = (argb << 8) + r;argb = (argb << 8) + g;argb = (argb << 8) + b;return argb;
    }
    public static double colorSeparation(int color1, int color2) {
        int r1 = separateARGB(color1, Colors.RGBCOMPONENTS.RED), r2 = separateARGB(color2, Colors.RGBCOMPONENTS.RED),
                g1 = separateARGB(color1, Colors.RGBCOMPONENTS.GREEN), g2 = separateARGB(color2, Colors.RGBCOMPONENTS.GREEN),
                b1 = separateARGB(color1, Colors.RGBCOMPONENTS.BLUE), b2 = separateARGB(color2, Colors.RGBCOMPONENTS.BLUE);
        return Math.sqrt((r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1));
    }
    public static int separateARGB(int color, int component) {
        return (color >> (component * 8)) & 0xFF;
    }
    public static int alphaBlend(int fore, int back, boolean gammaCorrection) {
        return alphaBlend(separateARGB(fore, Colors.RGBCOMPONENTS.ALPHA), fore, back, gammaCorrection);
    }
    private static int alphaBlend(int alpha, int fore, int back, boolean gammaCorrection) {
        return alphaBlend(alpha, 0xff, fore, back, gammaCorrection);
    }
    private static int alphaBlend(int alpha, int aMax, int fore, int back, boolean gammaCorrection) {
        return alphaBlend(alpha, aMax, separateARGB(fore, Colors.RGBCOMPONENTS.RED), separateARGB(fore, Colors.RGBCOMPONENTS.GREEN), separateARGB(fore, Colors.RGBCOMPONENTS.BLUE), back, gammaCorrection);
    }
    private static int alphaBlend(int a, int aMax, int r, int g, int b, int back, boolean gammaCorrection) {
        return alphaBlend(a, aMax, r, g, b, separateARGB(back, Colors.RGBCOMPONENTS.RED), separateARGB(back, Colors.RGBCOMPONENTS.GREEN), separateARGB(back, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
    }
    private static int alphaBlend(int a, int aMax, int r, int g, int b, int br, int bg, int bb, boolean gammaCorrection) {
        r = Colors.rgbToLinear(r, gammaCorrection);
        g = Colors.rgbToLinear(g, gammaCorrection);
        b = Colors.rgbToLinear(b, gammaCorrection);
        br = Colors.rgbToLinear(br, gammaCorrection);
        bg = Colors.rgbToLinear(bg, gammaCorrection);
        bb = Colors.rgbToLinear(bb, gammaCorrection);
        float alpha = ((float) a) / aMax;
        int nr = Math.round((1 - alpha) * br + alpha * r),
                ng = Math.round((1 - alpha) * bg + alpha * g),
                nb = Math.round((1 - alpha) * bb + alpha * b);
        return packARGB(Colors.linearToRgb(a, gammaCorrection), Colors.linearToRgb(nr, gammaCorrection), Colors.linearToRgb(ng, gammaCorrection), Colors.linearToRgb(nb, gammaCorrection));
    }
    public static int alphaBlend(int a, int r, int g, int b, int back, boolean gammaCorrection) {
        return alphaBlend(a, 0xff, r, g, b, back, gammaCorrection);
    }
    private static int alphaBlend(int a, int r, int g, int b, int br, int bg, int bb, boolean gammaCorrection) {
        return alphaBlend(a, 0xff, r, g, b, br, bg, bb, gammaCorrection);
    }
    public static int toGray(int common) {
        return toRGB(common, common, common);
    }
    public static int toRGB(int r, int g, int b) {
        if ((r < 0 || r > 255) || (g < 0 || g > 255) || (b < 0 || b > 255)) {
            r = boundsProtected(r, 256);
            g = boundsProtected(g, 256);
            b = boundsProtected(b, 256);
            //throw new IllegalArgumentException("R, G & B values must be between 0 to 255");
        }
        return packRGB(r, g, b);
    }
    public static int linearInterpolated(int fromcolor, int tocolor, int value, int maxvalue, int byParts,
                                         boolean gammaCorrection) {
        return linearInterpolated(fromcolor, tocolor, ((double) value) / maxvalue, byParts, gammaCorrection);
    }
    private static int lerpInt(int val1, int val2, double bias) {
        //bias = correctBias(bias);
        return Math.round((float) (val2 * bias + val1 * (1 - bias)));
    }
    public static int linearInterpolated(int color1, int color2, int color3, int value, int maxValue, int byparts,
                                         boolean gammaCorrection) {
        return interpolated(color1, color2, color3, (double) value / maxValue, byparts,
                InterpolationType.LINEAR, gammaCorrection);
    }
    public static int interpolated(int color1, int color2, int color3, double bias, int byparts,
                                   InterpolationType interpolationType, boolean gammaCorrection) {
        switch (interpolationType) {
            case LINEAR: {
                int colortmp1 = linearInterpolated(color1, color2, bias, byparts, gammaCorrection);
                int colortmp2 = linearInterpolated(color2, color3, bias, byparts, gammaCorrection);
                return linearInterpolated(colortmp1, colortmp2, bias, byparts, gammaCorrection);
            }
            case CATMULL_ROM_SPLINE: {
                int colortmp1 = splineInterpolated(color1, color2, bias, byparts, gammaCorrection);
                int colortmp2 = splineInterpolated(color2, color3, bias, byparts, gammaCorrection);
                return splineInterpolated(colortmp1, colortmp2, bias, byparts, gammaCorrection);
            }
            default:
                return Integer.MIN_VALUE;
        }
    }
    private static double correctBias(double bias) {
        bias = isNaN(bias) ? 0.0 : (isInfinite(bias) ? 1.0 : bias);
        return clamp(bias, 0.0, 1.0);
    }
    private static int linearInterpolated(int fromcolor, int tocolor, double bias, int byParts, boolean gammaCorrection) {
        bias = correctBias(bias);
        /*if (tocolor < fromcolor) {
            bias = 1 - bias;
        }*/
        if (byParts == 0) {
            int fr = Colors.rgbToLinear(separateARGB(fromcolor, Colors.RGBCOMPONENTS.RED), gammaCorrection);
            int fg = Colors.rgbToLinear(separateARGB(fromcolor, Colors.RGBCOMPONENTS.GREEN), gammaCorrection);
            int fb = Colors.rgbToLinear(separateARGB(fromcolor, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
            int tr = Colors.rgbToLinear(separateARGB(tocolor, Colors.RGBCOMPONENTS.RED), gammaCorrection);
            int tg = Colors.rgbToLinear(separateARGB(tocolor, Colors.RGBCOMPONENTS.GREEN), gammaCorrection);
            int tb = Colors.rgbToLinear(separateARGB(tocolor, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
            int nr = Colors.linearToRgb(lerpInt(fr, tr, bias), gammaCorrection);
            int ng = Colors.linearToRgb(lerpInt(fg, tg, bias), gammaCorrection);
            int nb = Colors.linearToRgb(lerpInt(fb, tb, bias), gammaCorrection);
            return toRGB(nr, ng, nb);
        } else if (byParts > 0) {
            /*int fa= separateARGB(fromcolor, Colors.RGBCOMPONENTS.ALPHA);
            int ta= separateARGB(tocolor, Colors.RGBCOMPONENTS.ALPHA);
            int na= Math.round((float) (ta * bias + fa * (1 - bias)));*/
            int na = boundsProtected((int) (bias * 255.0), 0xff);
            //na=(int)(bias*255.0);na=(na<0)?0:((na>255)?255:na);
            return alphaBlend(na, separateARGB(fromcolor, Colors.RGBCOMPONENTS.RED), separateARGB(fromcolor, Colors.RGBCOMPONENTS.GREEN), separateARGB(fromcolor, Colors.RGBCOMPONENTS.BLUE), separateARGB(tocolor, Colors.RGBCOMPONENTS.RED), separateARGB(tocolor, Colors.RGBCOMPONENTS.GREEN), separateARGB(tocolor, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
        } else {
            throw new IllegalArgumentException("Basic index-based interpolation needs to be done on an instance.");
            //return Math.round((float)tocolor+fromcolor/2);
        }
    }
    private static int splineInterpolated(int fromColor, int toColor, double bias, int byParts, boolean gammaCorrection) {
        bias = correctBias(bias);
        int nextColor = lerpInt(toColor, fromColor, bias), prevColor = lerpInt(toColor, fromColor, 1 - bias);
        return splineInterpolated(fromColor, toColor, max(prevColor, nextColor), min(prevColor, nextColor), bias, byParts, gammaCorrection);
    }
    private static int splineInterpolated(int color1, int color2, int color3, int color4, double bias, int byParts, boolean gammaCorrection) {
        bias = correctBias(bias);
        double h0 = 0.5 * (-(bias * bias) + (bias * bias * bias)),
                h1 = 0.5 * (bias + 4 * (bias * bias) - 3 * (bias * bias * bias)),
                h2 = 0.5 * (2 - 5 * (bias * bias) + 3 * (bias * bias * bias)),
                h3 = 0.5 * (-bias + 2 * (bias * bias) - (bias * bias * bias));
        if (byParts == 0) {
            int r1, r2, r3, r4, g1, g2, g3, g4, b1, b2, b3, b4;
            r1 = Colors.rgbToLinear(separateARGB(color1, Colors.RGBCOMPONENTS.RED), gammaCorrection);
            r2 = Colors.rgbToLinear(separateARGB(color2, Colors.RGBCOMPONENTS.RED), gammaCorrection);
            r3 = Colors.rgbToLinear(separateARGB(color3, Colors.RGBCOMPONENTS.RED), gammaCorrection);
            r4 = Colors.rgbToLinear(separateARGB(color4, Colors.RGBCOMPONENTS.RED), gammaCorrection);
            g1 = Colors.rgbToLinear(separateARGB(color1, Colors.RGBCOMPONENTS.GREEN), gammaCorrection);
            g2 = Colors.rgbToLinear(separateARGB(color2, Colors.RGBCOMPONENTS.GREEN), gammaCorrection);
            g3 = Colors.rgbToLinear(separateARGB(color3, Colors.RGBCOMPONENTS.GREEN), gammaCorrection);
            g4 = Colors.rgbToLinear(separateARGB(color4, Colors.RGBCOMPONENTS.GREEN), gammaCorrection);
            b1 = Colors.rgbToLinear(separateARGB(color1, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
            b2 = Colors.rgbToLinear(separateARGB(color2, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
            b3 = Colors.rgbToLinear(separateARGB(color3, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
            b4 = Colors.rgbToLinear(separateARGB(color4, Colors.RGBCOMPONENTS.BLUE), gammaCorrection);
            int nr = Colors.linearToRgb(Math.round((float) abs(h0 * r1 + h1 * r2 + h2 * r3 + h3 * r4)), gammaCorrection);
            int ng = Colors.linearToRgb(Math.round((float) abs(h0 * g1 + h1 * g2 + h2 * g3 + h3 * g4)), gammaCorrection);
            int nb = Colors.linearToRgb(Math.round((float) abs(h0 * b1 + h1 * b2 + h2 * b3 + h3 * b4)), gammaCorrection);
            return toRGB(nr, ng, nb);
        } else if (byParts > 0) {
            double alpha = abs((h0 + h1 + h2 + h3) * bias) * 255.0;
            //alpha=(alpha<0)?0:((alpha>255)?255:alpha);
            return alphaBlend(boundsProtected((int) alpha, 0xff),
                    separateARGB(color1, Colors.RGBCOMPONENTS.RED),
                    separateARGB(color1, Colors.RGBCOMPONENTS.GREEN),
                    separateARGB(color1, Colors.RGBCOMPONENTS.BLUE),
                    separateARGB(color2, Colors.RGBCOMPONENTS.RED),
                    separateARGB(color2, Colors.RGBCOMPONENTS.GREEN),
                    separateARGB(color2, Colors.RGBCOMPONENTS.BLUE),
                    gammaCorrection);
        } else {
            throw new IllegalArgumentException("Basic index-based interpolation needs to be done on an instance.");
            //return Math.round((float)tocolor+fromcolor/2);
        }
    }
    public static int interpolated(int fromColor, int toColor, double bias, int byParts,
                                   InterpolationType interpolationType, boolean gammaCorrection) {
        switch (interpolationType) {
            case LINEAR:
                return linearInterpolated(fromColor, toColor, bias, byParts, gammaCorrection);
            case CATMULL_ROM_SPLINE:
                return splineInterpolated(fromColor, toColor, bias, byParts, gammaCorrection);
            default:
                return Integer.MIN_VALUE;
        }
    }
    public int interpolatedColor(int color1, int color2, int color3, double bias) {
        return interpolated(color1, color2, color3, bias, byParts, interpolationType, gammaCorrection);
    }
    private int getInterpolationKind() {
        return interpolationKind;
    }
    private void setInterpolationKind(int interpolationKind) {
        this.interpolationKind = interpolationKind;
    }
    public boolean isGammaCorrection() {
        return gammaCorrection;
    }
    public void setGammaCorrection(boolean gammaCorrection) {
        this.gammaCorrection = gammaCorrection;
    }
    private boolean isSplineInterpolationCalcMode() {
        return splineInterpolationCalcMode;
    }
    private void setSplineInterpolationCalcMode(boolean splineInterpolationCalcMode) {
        this.splineInterpolationCalcMode = splineInterpolationCalcMode;
    }
    private boolean isSplineInterpolationResultMode() {
        return splineInterpolationResultMode;
    }
    private void setSplineInterpolationResultMode(boolean splineInterpolationResultMode) {
        this.splineInterpolationResultMode = splineInterpolationResultMode;
    }
    public InterpolationType getInterpolationType() {
        return interpolationType;
    }
    public void setInterpolationType(InterpolationType interpolationType) {
        this.interpolationType = interpolationType;
    }
    public int interpolated(int index1, int index2, int index3, double bias) {
        switch (interpolationType) {
            case LINEAR: {
                if (byParts < 0) {
                    return basicInterpolateIndex(index1, index2, bias);
                }
                return interpolated(getColor(index1), getColor(index2), getColor(index3), bias,
                        byParts, InterpolationType.LINEAR, gammaCorrection);
            }
            case CATMULL_ROM_SPLINE:
                return splineInterpolated(index1, index2, index3, bias);
            default:
                return Integer.MIN_VALUE;
        }
    }
    public int interpolated(int fromIndex, int toIndex, double bias) {
        switch (interpolationType) {
            case LINEAR: {
                if (byParts < 0) {
                    return basicInterpolateIndex(fromIndex, toIndex, bias);
                }
                return interpolated(getColor(fromIndex), getColor(toIndex), bias, byParts, InterpolationType.LINEAR, gammaCorrection);
            }
            case CATMULL_ROM_SPLINE:
                return splineInterpolated(fromIndex, toIndex, bias);
            default:
                return Integer.MIN_VALUE;
        }
    }
    public double getFractionalCount(int count, double fraction) {
        double u = scale * (count + weight * fraction * numColors);
        return u - (long) u;
    }
    public double getScale() {
        return scale;
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    private double getWeight() {
        return weight;
    }
    private void setWeight(double weight) {
        this.weight = weight;
    }
    public Complex getSmoothingBase() {
        return smoothingBase;
    }
    private void setSmoothingBase(Complex smoothingBase) {
        this.smoothingBase = smoothingBase;
    }
    private void setPalette(@NotNull int[] palette, boolean preserve) {
        if (!preserve) {
            this.palette = new int[palette.length];
            setNumColors(palette.length);
            System.arraycopy(palette, 0, this.palette, 0, palette.length);
        } else {
            @NotNull int[] tmpPalette = new int[this.palette.length];
            setNumColors(palette.length);
            System.arraycopy(this.palette, 0, tmpPalette, 0, this.palette.length);
            this.palette = new int[numColors];
            System.arraycopy(tmpPalette, 0, this.palette, 0, tmpPalette.length);
            System.arraycopy(palette, 0, this.palette, tmpPalette.length, this.palette.length - tmpPalette.length);
        }
    }
    private void init(Colors.MODE mode, int num_colors, int byParts, boolean logIndex, boolean cyclize,
                      InterpolationType interpolationType, boolean gammaCorrection,
                      boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode,
                      int interpolationKind) {
        colors_corrected = false;
        setByParts(byParts);
        setLogIndex(logIndex);
        setNumColors(num_colors);
        setExponentialSmoothing(true);
        alreadyCyclic = false;
        setCyclic(cyclize);
        //initRandomPalette(numColors, false);
        setMode(mode);
        setSmoothingBase(Complex.E);
        setInterpolationType(interpolationType);
        setGammaCorrection(gammaCorrection);
        setSplineInterpolationCalcMode(splineInterpolationCalcMode);
        setSplineInterpolationResultMode(splineInterpolationResultMode);
        setInterpolationKind(interpolationKind);
    }
    private void initRandomPalette(int num_colors, boolean preserve) {
        if (!preserve) {
            palette = new int[num_colors];
            for (int pidx = 0; pidx < num_colors; pidx++) {
                palette[pidx] = packRGB(((int) (Math.random() * 255)),
                        ((int) (Math.random() * 255)), ((int) (Math.random() * 255)));
            }
        } else {
            @NotNull int[] randtmp = new int[palette.length];
            System.arraycopy(palette, 0, randtmp, 0, palette.length);
            palette = new int[num_colors];
            System.arraycopy(randtmp, 0, palette, 0, randtmp.length);
            for (int pidx = randtmp.length; pidx < num_colors; pidx++) {
                palette[pidx] = packRGB(((int) (Math.random() * 255)), ((int) (Math.random() * 255)),
                        ((int) (Math.random() * 255)));
            }
        }
        if (isCyclic()) {
            makePaletteCyclic();
        }
    }
    private boolean isCyclic() {
        return cyclic;
    }
    private void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
        if (this.cyclic) {
            numColors /= 2;
        }
    }
    private void makePaletteCyclic() {
        if (alreadyCyclic) {
            numColors /= 2;
            numColors++;
            alreadyCyclic = false;
        }
        //mirror the palette about the last colour to make it cyclic.
        if (numColors < palette.length) {
            numColors = palette.length;
        }
        numColors = (palette.length * 2);
        @NotNull int[] tmp = new int[palette.length];
        System.arraycopy(palette, 0, tmp, 0, tmp.length);
        palette = new int[numColors];
        System.arraycopy(tmp, 0, palette, 0, tmp.length);
        //Since 2*tmp.length==numColors
        for (int i = numColors - 1, j = tmp.length - 1; i >= tmp.length && j >= 0; i--, j--) {
            palette[i] = palette[j];
        }
        alreadyCyclic = true;
    }
    private double getPhaseShift() {
        return phaseShift;
    }
    private void setPhaseShift(double phaseShift) {
        this.phaseShift = phaseShift;
    }
    private double getPeriodicity() {
        return periodicity;
    }
    private void setPeriodicity(double periodicity) {
        this.periodicity = periodicity;
    }
    private double transform(double index) {
        if (periodicity > 0) {
            double newIdx = index * periodicity + phaseShift;
            return newIdx - (long) newIdx;
        }
        return index;
    }
    public int basicInterpolateColor(int fromColor, int toColor, double bias) {
        if (logIndex) {
            return basicInterpolateIndex(indexOfColor(fromColor), indexOfColor(toColor), bias);
        } else {
            return basicInterpolateIndex(fromColor, toColor, bias);
        }
    }
    private int basicInterpolateIndex(int fromIndex, int toIndex, double bias) {
        if (fromIndex > toIndex) {
            int tmpIndex = fromIndex;
            fromIndex = toIndex;
            toIndex = tmpIndex;
        }
        return getColor(fromIndex + Math.round((float) (abs(toIndex - fromIndex) * bias)));
    }
    public int getColor(int index) {
        return palette[boundsProtected(index, palette.length)];
    }
    private void init(Colors.MODE mode, int color_density, int num_colors, int basecolor, int byParts,
                      boolean logIndex, boolean cyclize, InterpolationType interpolationType,
                      boolean gammaCorrection,
                      boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode,
                      int interpolationKind) {
        colors_corrected = false;
        setByParts(byParts);
        setLogIndex(logIndex);
        setColorDensity(color_density);
        setNumColors(num_colors);
        setBaseColor(basecolor);
        calcStep();
        setCyclic(cyclize);
        //initGradientPalette();
        setMode(mode);
        setExponentialSmoothing(true);
        setSmoothingBase(Complex.E);
        setInterpolationType(interpolationType);
        setGammaCorrection(gammaCorrection);
        setSplineInterpolationCalcMode(splineInterpolationCalcMode);
        setSplineInterpolationResultMode(splineInterpolationResultMode);
        setInterpolationKind(interpolationKind);
    }
    private void initGradientPalette() {
        palette = new int[numColors];
        if (step == 0) {
            initShadePalette();
            return;
        }
        int baseidx = numColors / 2;
        int increment = 0;
        for (int i = 0; i < baseidx; i++) {
            palette[i] = abs(baseColor - increment * step);
            increment++;
        }
        increment = 0;
        for (int i = baseidx; i < numColors; i++) {
            palette[i] = baseColor + increment * step;
            increment++;
        }
        if (isCyclic()) {
            makePaletteCyclic();
        }
    }
    private void initShadePalette() {
        int baseIndex = numColors / 2;
        for (int i = baseIndex - 1; i >= 0; i--) {
            palette[i] = getTint(baseColor, ((double) abs(baseIndex - i) / baseIndex));
        }
        for (int i = baseIndex; i < numColors; i++) {
            palette[i] = getShade(baseColor, ((double) abs(baseIndex - i) / baseIndex));
        }
        if (isCyclic()) {
            makePaletteCyclic();
        }
    }
    public int getTint(int color, double tint) {
        if (tint < 0.0 || tint > 1.0) {
            throw new IllegalArgumentException("Blending quantity out of range: " + tint);
        }
        tints += tint;
        tintCount++;
        int r = separateARGB(color, Colors.RGBCOMPONENTS.RED);
        int g = separateARGB(color, Colors.RGBCOMPONENTS.GREEN);
        int b = separateARGB(color, Colors.RGBCOMPONENTS.BLUE);
        int nr = boundsProtected(Math.round((float) (r + ((0xff - r) * tint))), 256);
        int ng = boundsProtected(Math.round((float) (g + ((0xff - g) * tint))), 256);
        int nb = boundsProtected(Math.round((float) (b + ((0xff - b) * tint))), 256);
        return packARGB(separateARGB(color, Colors.RGBCOMPONENTS.ALPHA), nr, ng, nb);
    }
    public double averageTint() {
        return tints / tintCount;
    }
    public int getShade(int color, double shade) {
        if (shade < 0.0 || shade > 1.0) {
            throw new IllegalArgumentException("Blending quantity out of range: " + shade);
        }
        tints += shade;
        tintCount++;
        int r = separateARGB(color, Colors.RGBCOMPONENTS.RED);
        int g = separateARGB(color, Colors.RGBCOMPONENTS.GREEN);
        int b = separateARGB(color, Colors.RGBCOMPONENTS.BLUE);
        int nr = boundsProtected(Math.round((float) (r * (1.0 - shade))), 256);
        int ng = boundsProtected(Math.round((float) (g * (1.0 - shade))), 256);
        int nb = boundsProtected(Math.round((float) (b * (1.0 - shade))), 256);
        return packARGB(separateARGB(color, Colors.RGBCOMPONENTS.ALPHA), nr, ng, nb);
    }
    private void calcStep() {
        setStep((baseColor / numColors) * colorDensity);
    }
    private void init(Colors.MODE mode, int color_density, int num_colors, int basecolor, int step, int byParts,
                      boolean logIndex, boolean cyclize, InterpolationType linearInterpolation, boolean gammaCorrection,
                      boolean splineInterpolationCalcMode, boolean splineInterpolationResultMode,
                      int interpolationType) {
        colors_corrected = false;
        setByParts(byParts);
        setLogIndex(logIndex);
        setColorDensity(color_density);
        setExponentialSmoothing(true);
        setCyclic(cyclize);
        setNumColors(num_colors);
        setBaseColor(basecolor);
        setStep(step);
        //initGradientPalette();
        setMode(mode);
        setSmoothingBase(Complex.E);
        setInterpolationType(linearInterpolation);
        setGammaCorrection(gammaCorrection);
        setSplineInterpolationCalcMode(splineInterpolationCalcMode);
        setSplineInterpolationResultMode(splineInterpolationResultMode);
        setInterpolationKind(interpolationType);
    }
    public boolean isLogIndex() {
        return logIndex;
    }
    private void setLogIndex(boolean logIndex) {
        this.logIndex = logIndex;
    }
    public int getByParts() {
        return byParts;
    }
    public void setByParts(int byParts) {
        this.byParts = byParts;
    }
    private int getBaseColor() {
        return baseColor;
    }
    private void setBaseColor(int baseColor) {
        this.baseColor = baseColor;
    }
    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    @Nullable
    public int[] getPalette() {
        return palette;
    }
    public int getColorDensity() {
        return colorDensity;
    }
    public void setColorDensity(int colorDensity) {
        if (colorDensity <= 0) {
            this.colorDensity = calculateColorDensity();
        } else {
            this.colorDensity = colorDensity;
        }
    }
    public void changeColorDensity(int colorDensity) {
        setColorDensity(clamp(colorDensity, numColors));
    }
    public int createIndexSimple(double val, double min, double max, int density) {
        return boundsProtected(round((float) ((val - min) / (max - min)) * density), numColors);
    }
    private int calculateColorDensity() {
        return numColors - 1;
    }
    public int getNumColors() {
        return numColors;
    }
    private void setNumColors(int numColors) {
        this.numColors = numColors;
    }
    public Colors.MODE getMode() {
        return mode;
    }
    public void setMode(Colors.MODE mode) {
        this.mode = mode;
    }
    public boolean isExponentialSmoothing() {
        return exponentialSmoothing;
    }
    private void setExponentialSmoothing(boolean exponentialSmoothing) {
        this.exponentialSmoothing = exponentialSmoothing;
    }
    @SuppressWarnings("unchecked")
    public void createSmoothPalette(@NotNull int[] control_colors, @NotNull double[] control_points) {
        if (alreadyCyclic) {
            numColors /= 2;
            numColors++;
            alreadyCyclic = false;
        }
        palette = new int[numColors];
        int byPartsBak = byParts;
        if (byParts < 0 && isAnyOf(mode, Colors.MODE.NEWTON_NORMALIZED_MODULUS, Colors.MODE.NEWTON_CLASSIC)) {
            byParts = 0;
        }
        if (interpolationType == InterpolationType.MONOTONE_CUBIC_SPLINE) {
            double[] red = new double[control_colors.length],
                    green = new double[control_colors.length],
                    blue = new double[control_colors.length];
            for (int i = 0; i < control_colors.length; ++i) {
                red[i] = separateARGB(control_colors[i], Colors.RGBCOMPONENTS.RED) / 255.0;
                green[i] = separateARGB(control_colors[i], Colors.RGBCOMPONENTS.GREEN) / 255.0;
                blue[i] = separateARGB(control_colors[i], Colors.RGBCOMPONENTS.BLUE) / 255.0;
            }
            Function<Double, Double>[] channelSplines = (Function<Double, Double>[]) new Function[]{
                    MathUtils.createInterpolant(control_points, red, ExtrapolationType.NONE),
                    MathUtils.createInterpolant(control_points, green, ExtrapolationType.NONE),
                    MathUtils.createInterpolant(control_points, blue, ExtrapolationType.NONE)
            };
            for (int i = 0; i < palette.length; ++i) {
                palette[i] = toRGB(
                        (int) MathUtils.clamp(Math.abs((channelSplines[0].apply((double) i / palette.length) * 255.0)), 0.0, 255.0),
                        (int) MathUtils.clamp(Math.abs((channelSplines[1].apply((double) i / palette.length) * 255.0)), 0.0, 255.0),
                        (int) MathUtils.clamp(Math.abs((channelSplines[2].apply((double) i / palette.length) * 255.0)), 0.0, 255.0)
                );
            }
            setInterpolationType(InterpolationType.CATMULL_ROM_SPLINE);
        } else {
            @NotNull int[] controls = new int[control_points.length];
            int color_density_backup = colorDensity;
            colorDensity = calculateColorDensity();
            for (int i = 0; i < controls.length && i < control_points.length; i++) {
                controls[i] = createIndex(control_points[i], 0, 1);
            }
            colorDensity = color_density_backup;
            final double diff = abs(palette.length - controls[controls.length - 1]), dist = diff + controls[0];
            for (int c = 0; c < controls.length - 1; ++c) {
                for (int i = controls[c], ctr = 0; i <= controls[c + 1]; ++i, ++ctr) {
                    double bias = ((double) ctr / abs(controls[c + 1] - controls[c]));
                    palette[i] = interpolated(control_colors[c], control_colors[c + 1], bias, byParts,
                            InterpolationType.LINEAR, gammaCorrection);
                }
            }
            for (int i = 0; i <= controls[0]; ++i) {
                palette[i] = interpolated(control_colors[controls.length - 1], control_colors[0], (i + diff) / dist,
                        byParts, InterpolationType.LINEAR, gammaCorrection);
            }
            for (int i = controls[controls.length - 1], ctr = 0; i < palette.length; ++i, ++ctr) {
                palette[i] = interpolated(control_colors[controls.length - 1], control_colors[0], ctr / dist,
                        byParts, InterpolationType.LINEAR, gammaCorrection);
            }
            byParts = byPartsBak;
        }
        if (isCyclic()) {
            makePaletteCyclic();
        }
    }
    public int createIndex(double val, double min, double max) {
        double resolution = 1.0 / numColors, adjustedResolution = multiplierThreshold * resolution, idx;
        if (((min <= ULP || abs(max - 1 - min) <= ULP || abs(max - min) <= ULP) && logIndex) || (!logIndex)) {
            idx = transform(abs((val - min) / (max - min)));
        } else {
            @NotNull Complex exp = new Complex(val / min);
            @NotNull Complex base = new Complex(max / min);
            idx = transform(divide(principallog(exp), principallog(base)).modulus());
        }
        idx = isNaN(idx) ? 0.0 : (isInfinite(idx) ? 1.0 : idx);
        //idx *= colorDensity;
        idx *= clamp(colorDensity, numColors);
        if (idx < adjustedResolution) {
            idx /= multiplierThreshold;
        }
        //return ((int) idx) % numColors;
        return boundsProtected(round((float) idx), numColors);
    }
    private int indexOfColor(int color) {
        for (int i = 0; i < palette.length; ++i) {
            if (palette[i] == color) {
                return i;
            }
        }
        return -1;
    }
    public int interpolated(int index, double bias) {
        bias = correctBias(bias);
        if (interpolationKind == 0) {
            return interpolated(index, lerpInt(index, palette.length - 1, bias), bias);
        } else if (interpolationKind > 0) {
            return interpolated(index, index > palette.length / 2 ? index - 1 : index + 1, bias);
        }
        return interpolated(index, index + 1, bias);
    }
    private int splineInterpolated(int index1, int index2, double bias) {
        bias = correctBias(bias);
        int index3, index4;
        if (index1 > index2) {
            if (splineInterpolationCalcMode) {
                index3 = lerpInt(index1, palette.length - 1, bias);
                index4 = lerpInt(0, index2, bias);
            } else {
                index3 = index1 + 1;
                index4 = index2 - 1;
            }
            if (splineInterpolationResultMode) {
                return splineInterpolated(index4, index2, index1, index3, bias);
            }
        } else {
            if (splineInterpolationCalcMode) {
                index3 = lerpInt(index2, palette.length - 1, bias);
                index4 = lerpInt(0, index1, bias);
            } else {
                index3 = index1 - 1;
                index4 = index2 + 1;
            }
            if (splineInterpolationResultMode) {
                return splineInterpolated(index4, index1, index2, index3, bias);
            }
        }
        return splineInterpolated(index1, index1, index3, index4, bias);
    }
    private int splineInterpolated(int index, int indexMin, int indexMax, double bias) {
        bias = correctBias(bias);
        int otherIdx = round((float) bias * (indexMax + indexMin));
        return splineInterpolated(indexMin, index, otherIdx, indexMax, bias);
    }
    private int splineInterpolated(int index1, int index2, int index3, int index4, double bias) {
        if ((!colors_corrected) && numColors < 4) {
            @NotNull int[] tmppalette = new int[numColors];
            if (palette != null) System.arraycopy(palette, 0, tmppalette, 0, palette.length);
            numColors = 4;
            init(mode, colorDensity, numColors, baseColor, step, byParts, logIndex, cyclic, interpolationType, gammaCorrection, splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
            if (palette != null && palette.length > 0) {
                int j = 0;
                for (int i = 0; i < numColors; i++) {
                    if (j == tmppalette.length - 1) {
                        j = 0;
                    }
                    palette[i] = tmppalette[j];
                }
            } else if (paletteType == Colors.PALETTE.RANDOM_PALETTE) {
                initRandomPalette(numColors, true);
            } else {
                initGradientPalette();
            }
            colors_corrected = true;
        }
        index1 = boundsProtected(index1, palette.length);
        index2 = boundsProtected(index2, palette.length);
        index3 = boundsProtected(index3, palette.length);
        index4 = boundsProtected(index4, palette.length);
        if (byParts >= 0) {
            return splineInterpolated(getColor(index1), getColor(index2), getColor(index3), getColor(index4), bias, byParts, gammaCorrection);
        } else {
            return basicInterpolateIndex(index1, index2, bias);
        }
    }
    private Colors.PALETTE getPaletteType() {
        return paletteType;
    }
    private void setPaletteType(Colors.PALETTE paletteType) {
        this.paletteType = paletteType;
    }
    public void fromString(@NotNull String[] colors) {
        setSmoothingBase(Complex.E);
        setExponentialSmoothing(true);
        setInterpolationType(InterpolationType.CATMULL_ROM_SPLINE);
        setPaletteType(Colors.PALETTE.valueOf(colors[0]));
        setMode(Colors.MODE.valueOf(colors[1]));
        setByParts(parseInt(colors[2]));
        @NotNull String[] smoothingData = split(colors[3], ";");
        if (smoothingData.length > 0) {
            setExponentialSmoothing(parseBoolean(smoothingData[0]));
        }
        if (smoothingData.length > 1) {
            setInterpolationType(InterpolationType.valueOf(smoothingData[1]));
        }
        if (smoothingData.length > 2) {
            setSmoothingBase(new Complex(smoothingData[2]));
        }
        setCyclic(parseBoolean(colors[4]));
        String[] modifiers = split(colors[5], ";");
        if (modifiers.length >= 1) {
            setLogIndex(parseBoolean(modifiers[0]));
            modifierEnabled = modifiers.length > 1 && parseBoolean(modifiers[1]);
        } else {
            setLogIndex(false);
            modifierEnabled = false;
        }
        setPeriodicity(parseDouble(colors[6]));
        setPhaseShift(parseDouble(colors[7]));
        setMultiplierThreshold(parseDouble(colors[8]));
        setScale(parseDouble(colors[9]));
        setWeight(parseDouble(colors[10]));
        @NotNull String[] interpolationConfig = split(colors[11], ";");
        setGammaCorrection(parseBoolean(interpolationConfig[0]));
        setSplineInterpolationCalcMode(parseBoolean(interpolationConfig[1]));
        setSplineInterpolationResultMode(parseBoolean(interpolationConfig[2]));
        setInterpolationKind(parseInt(interpolationConfig[3]));
        switch (paletteType) {
            case RANDOM_PALETTE:
                init(mode, parseInt(colors[12]), byParts, logIndex, cyclic,
                        interpolationType, gammaCorrection, splineInterpolationCalcMode,
                        splineInterpolationResultMode, interpolationKind);
                setColorDensity(parseInt(colors[13]));
                initRandomPalette(getNumColors(), false);
                break;
            case CUSTOM_PALETTE:
                @NotNull String[] parts = split(colors[12], ";");
                setColorDensity(parseInt(colors[13]));
                @NotNull int[] colorSet = new int[parts.length];
                for (int i = 0; i < colorSet.length; i++) {
                    colorSet[i] = parseInt(parts[i], 16);
                }
                setPalette(colorSet, false);
                break;
            case GRADIENT_PALETTE:
                switch (colors.length) {
                    case 16:
                        init(mode, parseInt(colors[12]), parseInt(colors[13]),
                                parseInt(colors[14], 16), parseInt(colors[15], 16),
                                byParts, logIndex, cyclic, interpolationType, gammaCorrection,
                                splineInterpolationCalcMode, splineInterpolationResultMode, interpolationKind);
                        break;
                    case 15:
                        init(mode, parseInt(colors[12]), parseInt(colors[13]),
                                parseInt(colors[14], 16), byParts, logIndex, cyclic,
                                interpolationType, gammaCorrection, splineInterpolationCalcMode,
                                splineInterpolationResultMode, interpolationKind);
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal Configuration String");
                }
                initGradientPalette();
                break;
            case SHADE_PALETTE:
                init(mode, parseInt(colors[12]), parseInt(colors[13]),
                        parseInt(colors[14], 16), 0xff000000, byParts, logIndex, cyclic,
                        interpolationType, gammaCorrection, splineInterpolationCalcMode,
                        splineInterpolationResultMode, interpolationKind);
                initShadePalette();
                break;
            case SMOOTH_PALETTE:
                init(mode, parseInt(colors[12]), byParts, logIndex, cyclic,
                        interpolationType, gammaCorrection, splineInterpolationCalcMode,
                        splineInterpolationResultMode, interpolationKind);
                setColorDensity(parseInt(colors[13]));
                @NotNull String[] controls = split(colors[14], ";");
                @NotNull int[] control_colors = new int[controls.length];
                @NotNull double[] control_points = new double[controls.length];
                for (int i = 0; i < controls.length; i++) {
                    @NotNull String[] control = split(controls[i], " ");
                    control_colors[i] = parseInt(control[0]);
                    control_points[i] = parseDouble(control[1]);
                }
                setPaletteType(Colors.PALETTE.CUSTOM_PALETTE);
                createSmoothPalette(control_colors, control_points);
                break;
            default:
                throw new IllegalArgumentException("Unsupported palette type");
        }
    }
    @Override
    public String toString() {
        StringBuilder representation = new StringBuilder(paletteType + "," + mode + "," + byParts + "," + exponentialSmoothing + ";" + interpolationType + ";" + smoothingBase + "," + logIndex + ";" + modifierEnabled + "," + periodicity + "," + phaseShift + "," + multiplierThreshold + "," + scale + "," + weight + "," + gammaCorrection + ";" + splineInterpolationCalcMode + ";" + splineInterpolationResultMode + ";" + interpolationKind);
        switch (paletteType) {
            case RANDOM_PALETTE:
                representation.append(",").append(numColors).append(",").append(colorDensity);
                break;
            case CUSTOM_PALETTE:
                for (int color : palette) {
                    representation.append(",").append(color);
                }
                representation.append(colorDensity);
                break;
            case GRADIENT_PALETTE:
            case SHADE_PALETTE:
                representation.append(",").append(colorDensity).append(",").append(numColors).append(",").append(baseColor).append((step == 0) ? "" : "," + step);
                break;
        }
        return representation.toString();
    }
    public boolean noCustomPalette() {
        return (paletteType == Colors.PALETTE.RANDOM_PALETTE) || (palette == null) || (palette.length == 0);
    }
    public boolean isModifierEnabled() {
        return modifierEnabled;
    }
    private void setModifierEnabled(boolean modifierEnabled) {
        this.modifierEnabled = modifierEnabled;
    }
    private double getMultiplierThreshold() {
        return multiplierThreshold;
    }
    private void setMultiplierThreshold(double multiplierThreshold) {
        this.multiplierThreshold = multiplierThreshold;
    }
}