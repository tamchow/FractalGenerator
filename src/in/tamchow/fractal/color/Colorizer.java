package in.tamchow.fractal.color;
import in.tamchow.fractal.helpers.annotations.NotNull;
import in.tamchow.fractal.helpers.annotations.Nullable;
import in.tamchow.fractal.math.complex.Complex;

import java.io.Serializable;

import static in.tamchow.fractal.helpers.math.MathUtils.*;
import static in.tamchow.fractal.helpers.strings.StringManipulator.split;
import static in.tamchow.fractal.math.complex.ComplexOperations.divide;
import static in.tamchow.fractal.math.complex.ComplexOperations.principallog;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.lang.Math.abs;
import static java.lang.Math.round;
/**
 * Holds colour configuration for  custom palettes
 */
public final class Colorizer implements Serializable {
    private static int tintCount = 0;
    private static double tints = 0;
    public Colors.MODE mode;
    @Nullable
    public int[] palette;
    private int basecolor, step, color_density, num_colors, byParts;
    private Complex smoothing_base;
    private double periodicity, phase_shift, multiplier_threshold = 1.0, scale = 10, weight = 1.0;
    private Colors.PALETTE palette_type;
    private boolean logIndex, exponentialSmoothing, cyclizeAble, modifierEnabled;
    private boolean colors_corrected, already_cyclized;
    public Colorizer(Colors.MODE mode, int color_density, int num_colors, int basecolor) {
        initColorConfig(mode, color_density, num_colors, basecolor, 0, false, false);
    }
    public Colorizer(Colors.MODE mode, int color_density, int num_colors, int basecolor, int byParts, boolean logIndex, boolean cyclize) {
        initColorConfig(mode, color_density, num_colors, basecolor, byParts, logIndex, cyclize);
    }
    public Colorizer(Colors.MODE mode, int color_density, int num_colors, int basecolor, int step, int byParts, boolean logIndex, boolean cyclize) {
        initColorConfig(mode, color_density, num_colors, basecolor, step, byParts, logIndex, cyclize);
    }
    public Colorizer(Colors.MODE mode, @NotNull int[] palette) {
        setPalette(palette, false);
        setMode(mode);
        setByParts(0);
    }
    public Colorizer(Colors.MODE mode, int color_density, int num_colors, int byParts, boolean logIndex, boolean cyclize) {
        this(mode, color_density, num_colors, byParts, logIndex, cyclize, 0);
    }
    public Colorizer(Colors.MODE mode, int color_density, int num_colors, int byParts, boolean logIndex, boolean cyclize, double periodicity) {
        this(mode, color_density, num_colors, byParts, logIndex, cyclize, periodicity, 0.0);
    }
    public Colorizer(Colors.MODE mode, int color_density, int num_colors, int byParts, boolean logIndex, boolean cyclize, double periodicity, double phase_shift) {
        initColorConfig(mode, num_colors, byParts, logIndex, cyclize);
        setColor_density(color_density);
        setPeriodicity(periodicity);
        setPhase_shift(phase_shift);
    }
    public Colorizer(Colors.MODE mode, int num_colors, int byParts, boolean logIndex, boolean cyclize) {
        initColorConfig(mode, num_colors, byParts, logIndex, cyclize);
        setColor_density(-1);
    }
    public Colorizer() {
        palette = null;
        initColorConfig(Colors.MODE.SIMPLE, 0, 0x0, 0, 0, false, false);
    }
    public Colorizer(@NotNull Colorizer old) {
        initColorConfig(old.getMode(), old.getColor_density(), old.getNum_colors(), old.getBasecolor(), old.getStep(), old.getByParts(), old.isLogIndex(), old.isCyclize());
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
    public static int alphaBlend(int fore, int back) {
        return alphaBlend(separateARGB(fore, Colors.RGBCOMPONENTS.ALPHA), fore, back);
    }
    public static int alphaBlend(int alpha, int fore, int back) {
        return alphaBlend(alpha, 0xff, fore, back);
    }
    public static int alphaBlend(int alpha, int amax, int fore, int back) {
        return alphaBlend(alpha, amax, separateARGB(fore, Colors.RGBCOMPONENTS.RED), separateARGB(fore, Colors.RGBCOMPONENTS.GREEN), separateARGB(fore, Colors.RGBCOMPONENTS.BLUE), back);
    }
    public static int alphaBlend(int a, int amax, int r, int g, int b, int back) {
        return alphaBlend(a, amax, r, g, b, separateARGB(back, Colors.RGBCOMPONENTS.RED), separateARGB(back, Colors.RGBCOMPONENTS.GREEN), separateARGB(back, Colors.RGBCOMPONENTS.BLUE));
    }
    public static int alphaBlend(int a, int amax, int r, int g, int b, int br, int bg, int bb) {
        float alpha = ((float) a) / amax;
        int nr = Math.round((1 - alpha) * br + alpha * r), ng = Math.round((1 - alpha) * bg + alpha * g), nb = Math.round((1 - alpha) * bb + alpha * b);
        return packARGB(a, nr, ng, nb);
    }
    public static int alphaBlend(int a, int r, int g, int b, int back) {
        return alphaBlend(a, 0xff, r, g, b, back);
    }
    public static int alphaBlend(int a, int r, int g, int b, int br, int bg, int bb) {
        return alphaBlend(a, 0xff, r, g, b, br, bg, bb);
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
    public static int linearInterpolated(int fromcolor, int tocolor, int value, int maxvalue, int byParts) {
        return linearInterpolated(fromcolor, tocolor, ((double) value) / maxvalue, byParts);
    }
    public static int linearInterpolated(int fromcolor, int tocolor, double bias, int byParts) {
        bias = (bias < 0) ? -bias : bias;
        bias = (bias > 1) ? bias - (long) bias : bias;
        if (tocolor < fromcolor) {
            bias = 1 - bias;
        }
        if (byParts == 0) {
            int fr = separateARGB(fromcolor, Colors.RGBCOMPONENTS.RED);
            int fg = separateARGB(fromcolor, Colors.RGBCOMPONENTS.GREEN);
            int fb = separateARGB(fromcolor, Colors.RGBCOMPONENTS.BLUE);
            int tr = separateARGB(tocolor, Colors.RGBCOMPONENTS.RED);
            int tg = separateARGB(tocolor, Colors.RGBCOMPONENTS.GREEN);
            int tb = separateARGB(tocolor, Colors.RGBCOMPONENTS.BLUE);
            int nr = Math.round((float) (tr * bias + fr * (1 - bias)));
            int ng = Math.round((float) (tg * bias + fg * (1 - bias)));
            int nb = Math.round((float) (tb * bias + fb * (1 - bias)));
            return toRGB(nr, ng, nb);
        } else if (byParts > 0) {
            /*int fa= separateARGB(fromcolor, Colors.RGBCOMPONENTS.ALPHA);
            int ta= separateARGB(tocolor, Colors.RGBCOMPONENTS.ALPHA);
            int na= Math.round((float) (ta * bias + fa * (1 - bias)));*/
            int na = boundsProtected((int) (bias * 255.0), 0xff);
            //na=(int)(bias*255.0);na=(na<0)?0:((na>255)?255:na);
            return alphaBlend(na, separateARGB(fromcolor, Colors.RGBCOMPONENTS.RED), separateARGB(fromcolor, Colors.RGBCOMPONENTS.GREEN), separateARGB(fromcolor, Colors.RGBCOMPONENTS.BLUE), separateARGB(tocolor, Colors.RGBCOMPONENTS.RED), separateARGB(tocolor, Colors.RGBCOMPONENTS.GREEN), separateARGB(tocolor, Colors.RGBCOMPONENTS.BLUE));
        } else {
            throw new IllegalArgumentException("Basic index-based interpolation needs to be done on an instance.");
            //return Math.round((float)tocolor+fromcolor/2);
        }
    }
    public double getFractionalCount(int count, double fraction) {
        return scale * (count + weight * fraction);
    }
    public double getScale() {
        return scale;
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }
    public Complex getSmoothing_base() {
        return smoothing_base;
    }
    public void setSmoothing_base(Complex smoothing_base) {
        this.smoothing_base = smoothing_base;
    }
    public void setPalette(@NotNull int[] palette, boolean preserve) {
        if (!preserve) {
            this.palette = new int[palette.length];
            setNum_colors(palette.length);
            System.arraycopy(palette, 0, this.palette, 0, palette.length);
        } else {
            @NotNull int[] tmpPalette = new int[this.palette.length];
            setNum_colors(palette.length);
            System.arraycopy(this.palette, 0, tmpPalette, 0, this.palette.length);
            this.palette = new int[num_colors];
            System.arraycopy(tmpPalette, 0, this.palette, 0, tmpPalette.length);
            System.arraycopy(palette, 0, this.palette, tmpPalette.length, this.palette.length - tmpPalette.length);
        }
    }
    private void initColorConfig(Colors.MODE mode, int num_colors, int byParts, boolean logIndex, boolean cyclize) {
        colors_corrected = false;
        setByParts(byParts);
        setLogIndex(logIndex);
        setNum_colors(num_colors);
        setExponentialSmoothing(true);
        already_cyclized = false;
        setCyclize(cyclize);
        initRandomPalette(num_colors, false);
        setMode(mode);
        setSmoothing_base(Complex.E);
    }
    private void initRandomPalette(int num_colors, boolean preserve) {
        if (!preserve) {
            palette = new int[num_colors];
            for (int pidx = 0; pidx < num_colors; pidx++) {
                palette[pidx] = packRGB(((int) (Math.random() * 255)), ((int) (Math.random() * 255)), ((int) (Math.random() * 255)));
            }
        } else {
            @NotNull int[] randtmp = new int[palette.length];
            System.arraycopy(palette, 0, randtmp, 0, palette.length);
            palette = new int[num_colors];
            System.arraycopy(randtmp, 0, palette, 0, randtmp.length);
            for (int pidx = randtmp.length; pidx < num_colors; pidx++) {
                palette[pidx] = packRGB(((int) (Math.random() * 255)), ((int) (Math.random() * 255)), ((int) (Math.random() * 255)));
            }
        }
        if (isCyclize()) {
            cyclizePalette();
        }
    }
    public boolean isCyclize() {
        return cyclizeAble;
    }
    public void setCyclize(boolean cyclize) {
        this.cyclizeAble = cyclize;
        if (this.cyclizeAble) {
            num_colors /= 2;
        }
    }
    private void cyclizePalette() {
        if (already_cyclized) {
            num_colors /= 2;
            num_colors++;
            already_cyclized = false;
        }
        //mirror the palette about the last colour to make it cyclic.
        if (num_colors < palette.length) {
            num_colors = palette.length;
        }
        num_colors = (palette.length * 2);
        @NotNull int[] tmp = new int[palette.length];
        System.arraycopy(palette, 0, tmp, 0, tmp.length);
        palette = new int[num_colors];
        System.arraycopy(tmp, 0, palette, 0, tmp.length);
        //Since 2*tmp.length==num_colors
        for (int i = num_colors - 1, j = tmp.length - 1; i >= tmp.length && j >= 0; i--, j--) {
            palette[i] = palette[j];
        }
        already_cyclized = true;
    }
    public double getPhase_shift() {
        return phase_shift;
    }
    public void setPhase_shift(double phase_shift) {
        this.phase_shift = phase_shift;
    }
    public double getPeriodicity() {
        return periodicity;
    }
    public void setPeriodicity(double periodicity) {
        this.periodicity = periodicity;
    }
    private double transform(double index) {
        if (periodicity > 0) {
            double newIdx = index * periodicity + phase_shift;
            return newIdx - (long) newIdx;
        }
        return index;
    }
    public int basicInterpolateColor(int fromColor, int toColor, double bias) {
        if (logIndex) {
            return basicInterpolateIndex(indexOfColor(fromColor), indexOfColor(toColor), bias);
        } else {
            if (fromColor > toColor) {
                int tmpIndex = fromColor;
                fromColor = toColor;
                toColor = tmpIndex;
            }
            return getColor(fromColor + Math.round((float) (abs(toColor - fromColor) * bias)));
        }
    }
    public int basicInterpolateIndex(int fromIndex, int toIndex, double bias) {
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
    private void initColorConfig(Colors.MODE mode, int color_density, int num_colors, int basecolor, int byParts, boolean logIndex, boolean cyclize) {
        colors_corrected = false;
        setByParts(byParts);
        setLogIndex(logIndex);
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        calcStep();
        setCyclize(cyclize);
        initGradientPalette();
        setMode(mode);
        setExponentialSmoothing(true);
        setSmoothing_base(Complex.E);
    }
    public void initGradientPalette() {
        palette = new int[num_colors];
        if (step == 0) {
            initShadePalette();
            return;
        }
        int baseidx = num_colors / 2;
        int increment = 0;
        for (int i = 0; i < baseidx; i++) {
            palette[i] = abs(basecolor - increment * step);
            increment++;
        }
        increment = 0;
        for (int i = baseidx; i < num_colors; i++) {
            palette[i] = basecolor + increment * step;
            increment++;
        }
        if (isCyclize()) {
            cyclizePalette();
        }
    }
    private void initShadePalette() {
        int baseidx = num_colors / 2;
        for (int i = baseidx - 1; i >= 0; i--) {
            palette[i] = getTint(basecolor, ((double) abs(baseidx - i) / baseidx));
        }
        for (int i = baseidx; i < num_colors; i++) {
            palette[i] = getShade(basecolor, ((double) abs(baseidx - i) / baseidx));
        }
        if (isCyclize()) {
            cyclizePalette();
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
        setStep((basecolor / num_colors) * color_density);
    }
    private void initColorConfig(Colors.MODE mode, int color_density, int num_colors, int basecolor, int step, int byParts, boolean logIndex, boolean cyclize) {
        colors_corrected = false;
        setByParts(byParts);
        setLogIndex(logIndex);
        setColor_density(color_density);
        setExponentialSmoothing(true);
        setCyclize(cyclize);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        setStep(step);
        initGradientPalette();
        setMode(mode);
        setSmoothing_base(Complex.E);
    }
    public boolean isLogIndex() {
        return logIndex;
    }
    public void setLogIndex(boolean logIndex) {
        this.logIndex = logIndex;
    }
    public int getByParts() {
        return byParts;
    }
    public void setByParts(int byParts) {
        this.byParts = byParts;
    }
    public int getBasecolor() {
        return basecolor;
    }
    public void setBasecolor(int basecolor) {
        this.basecolor = basecolor;
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
    public int getColor_density() {
        return color_density;
    }
    public void setColor_density(int color_density) {
        if (color_density <= 0) {
            this.color_density = calculateColorDensity();
        } else {
            this.color_density = color_density;
        }
    }
    public void changeColorDensity(int color_density) {
        setColor_density(clamp(color_density, num_colors));
    }
    public int createIndexSimple(double val, double min, double max) {
        return boundsProtected(round((float) ((val - min) / (max - min)) * color_density), num_colors);
    }
    public int calculateColorDensity() {
        return num_colors - 1;
    }
    public int getNum_colors() {
        return num_colors;
    }
    public void setNum_colors(int num_colors) {
        this.num_colors = num_colors;
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
    public void setExponentialSmoothing(boolean exponentialSmoothing) {
        this.exponentialSmoothing = exponentialSmoothing;
    }
    public void createSmoothPalette(int[] control_colors, @NotNull double[] control_points) {
        createSmoothPalette(control_colors, control_points, false);
    }
    public void createSmoothPalette(int[] control_colors, @NotNull double[] control_points, boolean useSpline) {
        if (already_cyclized) {
            num_colors /= 2;
            num_colors++;
            already_cyclized = false;
        }
        palette = new int[num_colors];
        @NotNull int[] controls = new int[control_points.length];
        int color_density_backup = color_density;
        color_density = num_colors;
        for (int i = 0; i < controls.length && i < control_points.length; i++) {
            controls[i] = createIndex(control_points[i], 0, 1);
        }
        color_density = color_density_backup;
        int c = 0;
        for (int i = 0; i < palette.length && c < controls.length; i++) {
            if (i == controls[c]) {
                palette[i] = control_colors[c];
                c++;
            }
        }
        c = 0;
        for (int i = 0, cnext = c + 1; i < palette.length && c < controls.length; i++) {
            if (c == controls.length - 1) {
                cnext = 0;
            }
            if (i == controls[c]) {
                c++;
                cnext++;
                continue;
            }
            if (useSpline) {
                int c_p = (c == 0) ? controls.length - 1 : c - 1, c_n = (cnext == controls.length - 1) ? 0 : cnext + 1;
                palette[i] = splineInterpolated(controls[c], controls[cnext], controls[c_p], controls[c_n], ((double) abs(i - controls[c])) / abs(controls[cnext] - controls[c]));
            } else {
                palette[i] = linearInterpolated(control_colors[c], control_colors[cnext], abs(i - controls[c]), abs(controls[cnext] - controls[c]), getByParts());
            }
        }
        if (isCyclize()) {
            cyclizePalette();
        }
    }
    public int createIndex(double val, double min, double max) {
        double resolution = 1.0 / num_colors, adjustedResolution = multiplier_threshold * resolution, idx;
        if (((min <= ULP || abs(max - 1 - min) <= ULP || abs(max - min) <= ULP) && logIndex) || (!logIndex)) {
            idx = transform(abs((val - min) / (max - min)));
        } else {
            @NotNull Complex exp = new Complex(val / min);
            @NotNull Complex base = new Complex(max / min);
            idx = transform(divide(principallog(exp), principallog(base)).modulus());
        }
        idx = isNaN(idx) ? 0.0 : (isInfinite(idx) ? 1.0 : idx);
        //idx *= color_density;
        idx *= clamp(color_density, num_colors);
        if (idx < adjustedResolution) {
            idx /= multiplier_threshold;
        }
        //return ((int) idx) % num_colors;
        return boundsProtected(round((float) idx), num_colors);
    }
    public int indexOfColor(int color) {
        for (int i = 0; i < palette.length; ++i) {
            if (palette[i] == color) {
                return i;
            }
        }
        return -1;
    }
    public int splineInterpolated(int index, double bias) {
        return splineInterpolated(index, index + 1, bias);
    }
    public int splineInterpolated(int index, int index1, double bias) {
        int i2, i3;
        if (index > index1) {
            i2 = index + 1;
            i3 = index1 - 1;
        } else {
            i2 = index1 + 1;
            i3 = index - 1;
        }
        return splineInterpolated(index, index1, i2, i3, bias);
    }
    public int splineInterpolated(int index, int index1, int i2, int i3, double bias) {
        if ((!colors_corrected) && num_colors < 4) {
            @NotNull int[] tmppalette = new int[num_colors];
            if (palette != null) System.arraycopy(palette, 0, tmppalette, 0, palette.length);
            num_colors = 4;
            initColorConfig(mode, color_density, num_colors, basecolor, step, byParts, logIndex, cyclizeAble);
            if (palette != null && palette.length > 0) {
                int j = 0;
                for (int i = 0; i < num_colors; i++) {
                    if (j == tmppalette.length - 1) {
                        j = 0;
                    }
                    palette[i] = tmppalette[j];
                }
            } else if (palette_type == Colors.PALETTE.RANDOM_PALETTE) {
                initRandomPalette(num_colors, true);
            } else {
                initGradientPalette();
            }
            colors_corrected = true;
        }
        bias = (bias < 0) ? -bias : bias;
        bias = (bias > 1) ? bias - (long) bias : bias;
        double h0 = 0.5 * ((bias * bias) * (bias - 1)),
                h1 = 0.5 * (bias * (1 + 4 * bias - 3 * (bias * bias))),
                h2 = 0.5 * (2 - 5 * (bias * bias) + 3 * (bias * bias * bias)),
                h3 = 0.5 * (bias * (2 * bias - (bias * bias) - 1));
        index = boundsProtected(index, palette.length);
        index1 = boundsProtected(index1, palette.length);
        i2 = boundsProtected(i2, palette.length);
        i3 = boundsProtected(i3, palette.length);
        if (byParts == 0) {
            int r1, r2, r3, r4, g1, g2, g3, g4, b1, b2, b3, b4;
            r1 = separateARGB(getColor(index), Colors.RGBCOMPONENTS.RED);
            r2 = separateARGB(getColor(index1), Colors.RGBCOMPONENTS.RED);
            r3 = separateARGB(getColor(i2), Colors.RGBCOMPONENTS.RED);
            r4 = separateARGB(getColor(i3), Colors.RGBCOMPONENTS.RED);
            g1 = separateARGB(getColor(index), Colors.RGBCOMPONENTS.GREEN);
            g2 = separateARGB(getColor(index1), Colors.RGBCOMPONENTS.GREEN);
            g3 = separateARGB(getColor(i2), Colors.RGBCOMPONENTS.GREEN);
            g4 = separateARGB(getColor(i3), Colors.RGBCOMPONENTS.GREEN);
            b1 = separateARGB(getColor(index), Colors.RGBCOMPONENTS.BLUE);
            b2 = separateARGB(getColor(index1), Colors.RGBCOMPONENTS.BLUE);
            b3 = separateARGB(getColor(i2), Colors.RGBCOMPONENTS.BLUE);
            b4 = separateARGB(getColor(i3), Colors.RGBCOMPONENTS.BLUE);
            int nr = Math.round((float) abs(h0 * r1 + h1 * r2 + h2 * r3 + h3 * r4));
            int ng = Math.round((float) abs(h0 * g1 + h1 * g2 + h2 * g3 + h3 * g4));
            int nb = Math.round((float) abs(h0 * b1 + h1 * b2 + h2 * b3 + h3 * b4));
            return toRGB(nr, ng, nb);
        } else if (byParts > 0) {
            double alpha = abs((h0 + h1 + h2 + h3) * bias) * 255.0;
            //alpha=(alpha<0)?0:((alpha>255)?255:alpha);
            return alphaBlend(boundsProtected((int) alpha, 0xff), separateARGB(palette[index], Colors.RGBCOMPONENTS.RED), separateARGB(palette[index], Colors.RGBCOMPONENTS.GREEN), separateARGB(palette[index], Colors.RGBCOMPONENTS.BLUE), separateARGB(palette[index1], Colors.RGBCOMPONENTS.RED), separateARGB(palette[index1], Colors.RGBCOMPONENTS.GREEN), separateARGB(palette[index1], Colors.RGBCOMPONENTS.BLUE));
        } else {
            return basicInterpolateIndex(index, index1, bias);
        }
    }
    public Colors.PALETTE getPalette_type() {
        return palette_type;
    }
    public void setPalette_type(Colors.PALETTE palette_type) {
        this.palette_type = palette_type;
    }
    public void fromString(@NotNull String[] colors) {
        setSmoothing_base(Complex.E);
        setExponentialSmoothing(true);
        setPalette_type(Colors.PALETTE.valueOf(colors[0]));
        setMode(Colors.MODE.valueOf(colors[1]));
        setByParts(Integer.valueOf(colors[2]));
        @NotNull String[] smoothingData = split(colors[3], ";");
        if (smoothingData.length > 0) {
            setExponentialSmoothing(Boolean.valueOf(smoothingData[0]));
        }
        if (smoothingData.length > 1) {
            setSmoothing_base(new Complex(smoothingData[1]));
        }
        setCyclize(Boolean.valueOf(colors[4]));
        String[] modifiers = split(colors[5], ";");
        if (modifiers.length >= 1) {
            setLogIndex(Boolean.valueOf(modifiers[0]));
            if (modifiers.length > 1) {
                modifierEnabled = Boolean.valueOf(modifiers[1]);
            } else {
                modifierEnabled = false;
            }
        } else {
            setLogIndex(false);
            modifierEnabled = false;
        }
        setPeriodicity(Double.valueOf(colors[6]));
        setPhase_shift(Double.valueOf(colors[7]));
        setMultiplier_threshold(Double.valueOf(colors[8]));
        setScale(Double.valueOf(colors[9]));
        setWeight(Double.valueOf(colors[10]));
        switch (palette_type) {
            case RANDOM_PALETTE:
                initColorConfig(mode, Integer.valueOf(colors[11]), byParts, logIndex, cyclizeAble);
                setColor_density(Integer.valueOf(colors[12]));
                break;
            case CUSTOM_PALETTE:
                @NotNull String[] parts = split(colors[11], ";");
                setColor_density(Integer.valueOf(colors[12]));
                @NotNull int[] colorset = new int[parts.length];
                for (int i = 0; i < colorset.length; i++) {
                    colorset[i] = Integer.valueOf(parts[i], 16);
                }
                setPalette(colorset, false);
                break;
            case GRADIENT_PALETTE:
                if (colors.length == 15) {
                    initColorConfig(mode, Integer.valueOf(colors[11]), Integer.valueOf(colors[12]), Integer.valueOf(colors[13], 16), Integer.valueOf(colors[14], 16), byParts, logIndex, cyclizeAble);
                } else if (colors.length == 14) {
                    initColorConfig(mode, Integer.valueOf(colors[11]), Integer.valueOf(colors[12]), Integer.valueOf(colors[13], 16), byParts, logIndex, cyclizeAble);
                }
                break;
            case SHADE_PALETTE:
                initColorConfig(mode, Integer.valueOf(colors[11]), Integer.valueOf(colors[12]), Integer.valueOf(colors[13], 16), 0xff000000, byParts, logIndex, cyclizeAble);
                break;
            case SMOOTH_PALETTE_LINEAR:
            case SMOOTH_PALETTE_SPLINE:
                initColorConfig(mode, Integer.valueOf(colors[11]), byParts, logIndex, cyclizeAble);
                setColor_density(Integer.valueOf(colors[12]));
                @NotNull String[] controls = split(colors[13], ";");
                @NotNull int[] control_colors = new int[controls.length];
                @NotNull double[] control_points = new double[controls.length];
                for (int i = 0; i < controls.length; i++) {
                    @NotNull String[] control = split(controls[i], " ");
                    control_colors[i] = Integer.valueOf(control[0]);
                    control_points[i] = Double.valueOf(control[1]);
                }
                if (palette_type == Colors.PALETTE.SMOOTH_PALETTE_SPLINE) {
                    createSmoothPalette(control_colors, control_points, true);
                } else {
                    createSmoothPalette(control_colors, control_points, false);
                }
                setPalette_type(Colors.PALETTE.CUSTOM_PALETTE);
                break;
            default:
                throw new IllegalArgumentException("Unsupported palette type");
        }
    }
    @Override
    public String toString() {
        String representation = palette_type + "," + mode + "," + byParts + "," + exponentialSmoothing + "," + logIndex + ";" + modifierEnabled + "," + periodicity + "," + phase_shift + "," + multiplier_threshold + "," + scale + "," + weight;
        switch (palette_type) {
            case RANDOM_PALETTE:
                representation += "," + num_colors + "," + color_density;
                break;
            case CUSTOM_PALETTE:
                for (int color : palette) {
                    representation += "," + color;
                }
                representation += color_density;
                break;
            case GRADIENT_PALETTE:
            case SHADE_PALETTE:
                representation += "," + color_density + "," + num_colors + "," + basecolor + ((step == 0) ? "" : "," + step);
                break;
        }
        return representation;
    }
    public boolean noCustomPalette() {
        return (palette_type == Colors.PALETTE.RANDOM_PALETTE) || (palette == null) || (palette.length == 0);
    }
    public boolean isModifierEnabled() {
        return modifierEnabled;
    }
    public double getMultiplier_threshold() {
        return multiplier_threshold;
    }
    public void setMultiplier_threshold(double multiplier_threshold) {
        this.multiplier_threshold = multiplier_threshold;
    }
}