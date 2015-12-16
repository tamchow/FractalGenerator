package in.tamchow.fractal.color;
import in.tamchow.fractal.math.MathUtils;

import java.io.Serializable;
/**
 * Holds colour configuration for  custom palettes
 */
public class ColorConfig implements Serializable {
    public int basecolor, step, color_density, num_colors, mode, palette_type;
    public int[] palette;
    public boolean byParts;
    boolean colors_corrected;
    public ColorConfig(int mode, int color_density, int num_colors, int basecolor) {
        initColorConfig(mode, color_density, num_colors, basecolor, false);}
    private void initColorConfig(int mode, int color_density, int num_colors, int basecolor, boolean byParts) {
        colors_corrected = false; setByParts(byParts);
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        calcStep();
        initGradientPalette(); setMode(mode);
    }
    public void initGradientPalette() {
        setPalette_type(Colors.PALETTE.CUSTOM);
        palette = new int[num_colors];
        if (step == 0) {
            initShadePalette(); return;
        }
        int baseidx = num_colors / 2; int increment = 0;
        for (int i = 0; i < baseidx; i++) {
            palette[i] = Math.abs(basecolor - increment * step); increment++;
        } increment = 0;
        for (int i = baseidx; i < num_colors; i++) {
            palette[i] = basecolor + increment * step; increment++;
        }
    }
    private void initShadePalette() {
        int baseidx = num_colors / 2; for (int i = baseidx - 1; i >= 0; i--) {
            palette[i] = getTint(basecolor, ((double) Math.abs(baseidx - i) / baseidx));
        }
        for (int i = baseidx; i < num_colors; i++) {
            palette[i] = getShade(basecolor, ((double) Math.abs(baseidx - i) / baseidx));
        }
    }
    public int getTint(int color, double tint) {
        int r = separateRGB(color, Colors.RGBCOMPONENTS.RED); int g = separateRGB(color, Colors.RGBCOMPONENTS.GREEN);
        int b = separateRGB(color, Colors.RGBCOMPONENTS.BLUE);
        int nr = (int) (r + (255 - r) * tint); int ng = (int) (g + (255 - g) * tint);
        int nb = (int) (b + (255 - b) * tint); return (nr << 16) | (ng << 8) | (nb);
    }
    public static int separateRGB(int color, int component) {
        int part = 0; switch (component) {
            case Colors.RGBCOMPONENTS.RED: part = (color >> 16) & 0xFF; break;
            case Colors.RGBCOMPONENTS.GREEN: part = (color >> 8) & 0xFF; break;
            case Colors.RGBCOMPONENTS.BLUE: part = (color) & 0xFF; break;
            default: throw new IllegalArgumentException("Cannot extract unknown part of 24-bit packed RGB");
        } return part;
    }
    public int getShade(int color, double shade) {
        int r = separateRGB(color, Colors.RGBCOMPONENTS.RED); int g = separateRGB(color, Colors.RGBCOMPONENTS.GREEN);
        int b = separateRGB(color, Colors.RGBCOMPONENTS.BLUE);
        int nr = (int) (r * (1 - shade)); int ng = (int) (g * (1 - shade)); int nb = (int) (b * (1 - shade));
        return (nr << 16) | (ng << 8) | (nb);
    }
    private void calcStep() {
        setStep((0xfffff / (color_density << num_colors)));
    }
    public ColorConfig(int mode, int color_density, int num_colors, int basecolor, boolean byParts) {
        initColorConfig(mode, color_density, num_colors, basecolor, byParts);
    }
    public ColorConfig(int mode, int color_density, int num_colors, int basecolor, int step, boolean byParts) {
        initColorConfig(mode, color_density, num_colors, basecolor, step, byParts);
    }
    private void initColorConfig(int mode, int color_density, int num_colors, int basecolor, int step, boolean byParts) {
        colors_corrected = false; this.byParts = byParts;
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        setStep(step);
        initGradientPalette();
        setMode(mode);
    }
    public ColorConfig(int mode, int[] palette) {
        setPalette(palette, false);
        setMode(mode); setByParts(false);
    }
    public void setPalette(int[] palette, boolean preserve) {
        if (!preserve) {
            this.palette = new int[palette.length]; setNum_colors(palette.length);
            System.arraycopy(palette, 0, this.palette, 0, palette.length);
        } else {
            int[] tmpPalette = new int[this.palette.length]; setNum_colors(palette.length);
            System.arraycopy(this.palette, 0, tmpPalette, 0, this.palette.length);
            this.palette = new int[num_colors];
            System.arraycopy(tmpPalette, 0, this.palette, 0, tmpPalette.length);
            System.arraycopy(palette, 0, this.palette, tmpPalette.length, this.palette.length - tmpPalette.length);
        }
    }
    public ColorConfig(int mode, int color_density, int num_colors) {
        initColorConfig(mode, num_colors, false); setColor_density(color_density);
    }
    private void initColorConfig(int mode, int num_colors, boolean byParts) {
        colors_corrected = false; setByParts(byParts);
        setNum_colors(num_colors);
        initRandomPalette(num_colors, false); setMode(mode);
    }
    public void initRandomPalette(int num_colors, boolean preserve) {
        setPalette_type(Colors.PALETTE.RANDOM);
        if (!preserve) {
            palette = new int[num_colors];
            for (int pidx = 0; pidx < num_colors; pidx++) {
                palette[pidx] = (((int) (Math.random() * 255)) << 16 | ((int) (Math.random() * 255)) << 8 | ((int) (Math.random() * 255)));
            }
        } else {
            int[] randtmp = new int[palette.length];
            System.arraycopy(palette, 0, randtmp, 0, palette.length);
            palette = new int[num_colors];
            System.arraycopy(randtmp, 0, palette, 0, randtmp.length);
            for (int pidx = randtmp.length; pidx < num_colors; pidx++) {
                palette[pidx] = (((int) (Math.random() * 255)) << 16 | ((int) (Math.random() * 255)) << 8 | ((int) (Math.random() * 255)));
            }
        }
    }
    public ColorConfig() {
        palette = null;
        setPalette_type(Colors.PALETTE.RANDOM); initColorConfig(0, 0, 0x0, 0, false);
    }
    public ColorConfig(ColorConfig old) {
        initColorConfig(old.getMode(), old.getColor_density(), old.getNum_colors(), old.getBasecolor(), old.getStep(), old.isByParts());
        setPalette(old.getPalette(), false);
        colors_corrected = old.colors_corrected;
    }
    public boolean isByParts() {
        return byParts;
    }
    public void setByParts(boolean byParts) {
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
    public int[] getPalette() {
        return palette;
    }
    public int getColor_density() {
        return color_density;
    }
    public void setColor_density(int color_density) {
        this.color_density = color_density;
    }
    public int getNum_colors() {
        return num_colors;
    }
    public void setNum_colors(int num_colors) {
        this.num_colors = num_colors;
    }
    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public static int linearInterpolated(int fromcolor, int tocolor, int value, int maxvalue, boolean byParts) {
        return linearInterpolated(fromcolor, tocolor, ((double) value) / maxvalue, byParts);
    }
    public static int linearInterpolated(int fromcolor, int tocolor, double bias, boolean byParts) {
        if (byParts) {
            int fr = separateRGB(fromcolor, Colors.RGBCOMPONENTS.RED);
            int fg = separateRGB(fromcolor, Colors.RGBCOMPONENTS.GREEN);
            int fb = separateRGB(fromcolor, Colors.RGBCOMPONENTS.BLUE);
            int tr = separateRGB(tocolor, Colors.RGBCOMPONENTS.RED);
            int tg = separateRGB(tocolor, Colors.RGBCOMPONENTS.GREEN);
            int tb = separateRGB(tocolor, Colors.RGBCOMPONENTS.BLUE); int nr = (int) (tr * bias + fr * (1 - bias));
            int ng = (int) (tg * bias + fg * (1 - bias)); int nb = (int) (tb * bias + fb * (1 - bias));
            return nr << 16 | ng << 8 | nb;
        } return (int) (tocolor * bias + fromcolor * (1 - bias));
    }
    public int calculateColorDensity() {return MathUtils.firstPrimeFrom(num_colors);}
    public int createIndex(double val, double min, double max) {
        return (int) Math.abs(((((val - min) / (max - min))) * color_density) % num_colors);
    }
    public int getColor(int index) {
        return palette[index];
    }
    public int splineInterpolated(int index, double bias) {
        if ((!colors_corrected) && num_colors < 4) {
            num_colors = 4; initColorConfig(mode, color_density, num_colors, basecolor, step, byParts);
            if (palette_type == Colors.PALETTE.RANDOM) {
                initRandomPalette(num_colors, true);
            } else {initGradientPalette();} colors_corrected = true;
        }
        double h0 = 0.5 * ((bias * bias) * (bias - 1)),
                h1 = 0.5 * (bias * (1 + 4 * bias - 3 * (bias * bias))),
                h2 = 0.5 * (2 - 5 * (bias * bias) + 3 * (bias * bias * bias)),
                h3 = 0.5 * (bias * (2 * bias - (bias * bias) - 1));
        int i1 = ((index - 1) < 0) ? num_colors - 1 : index - 1, i2 = ((index - 2) < 0) ? num_colors - 2 : index - 2, i3 = ((index - 3) < 0) ? num_colors - 3 : index - 3;
        if (byParts) {
            int r1, r2, r3, r4, g1, g2, g3, g4, b1, b2, b3, b4;
            r1 = separateRGB(palette[index], Colors.RGBCOMPONENTS.RED);
            r2 = separateRGB(palette[i1], Colors.RGBCOMPONENTS.RED);
            r3 = separateRGB(palette[i2], Colors.RGBCOMPONENTS.RED);
            r4 = separateRGB(palette[i3], Colors.RGBCOMPONENTS.RED);
            g1 = separateRGB(palette[index], Colors.RGBCOMPONENTS.GREEN);
            g2 = separateRGB(palette[i1], Colors.RGBCOMPONENTS.GREEN);
            g3 = separateRGB(palette[i2], Colors.RGBCOMPONENTS.GREEN);
            g4 = separateRGB(palette[i3], Colors.RGBCOMPONENTS.GREEN);
            b1 = separateRGB(palette[index], Colors.RGBCOMPONENTS.BLUE);
            b2 = separateRGB(palette[i1], Colors.RGBCOMPONENTS.BLUE);
            b3 = separateRGB(palette[i2], Colors.RGBCOMPONENTS.BLUE);
            b4 = separateRGB(palette[i3], Colors.RGBCOMPONENTS.BLUE);
            int nr = (int) Math.abs(h0 * r1 + h1 * r2 + h2 * r3 + h3 * r4);
            int ng = (int) Math.abs(h0 * g1 + h1 * g2 + h2 * g3 + h3 * g4);
            int nb = (int) Math.abs(h0 * b1 + h1 * b2 + h2 * b3 + h3 * b4); return nr << 16 | ng << 8 | nb;
        }
        double color = (h0 * palette[index] + h1 * palette[i1] + h2 * palette[i2] + h3 * palette[i3]);
        color = (color < 0) ? -color : color; return (int) color;
    }
    public int getPalette_type() {
        return palette_type;
    }
    public void setPalette_type(int palette_type) {
        this.palette_type = palette_type;
    }
    public void fromString(String[] colors) {
        String type = colors[0]; mode = Integer.valueOf(colors[1]); byParts = Boolean.valueOf(colors[2]);
        switch (type) {
            case "RANDOM_PALETTE": initColorConfig(mode, Integer.valueOf(colors[3]), byParts); break;
            case "CUSTOM_PALETTE": String[] parts = colors[3].split(";"); int[] colorset = new int[parts.length]; for (int i = 0; i < colorset.length; i++) {
                colorset[i] = Integer.valueOf(parts[i], 16);
            } setPalette(colorset, false); setColor_density(Integer.valueOf(parts[4])); break;
            case "GRADIENT_PALETTE": if (colors.length == 7) {
                initColorConfig(mode, Integer.valueOf(colors[3]), Integer.valueOf(colors[4]), Integer.valueOf(colors[5], 16), Integer.valueOf(colors[6], 16), byParts);
            } else if (colors.length == 6) {
                initColorConfig(mode, Integer.valueOf(colors[3]), Integer.valueOf(colors[4]), Integer.valueOf(colors[5], 16), byParts);
            } break;
            case "SHADE_PALETTE": initColorConfig(mode, Integer.valueOf(colors[3]), Integer.valueOf(colors[4]), Integer.valueOf(colors[5], 16), 0x000000, byParts); break;
            default: throw new IllegalArgumentException("Unsupported palette type");
        }
    }
    public boolean noCustomPalette() {
        return (palette_type == Colors.PALETTE.RANDOM) || (palette.length == 0) || (palette == null);
    }
}
