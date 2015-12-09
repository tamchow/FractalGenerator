package in.tamchow.fractal.color;
/**
 * Holds colour configuration for  custom palettes
 */
public class ColorConfig {
    public int basecolor, step, color_density, num_colors, mode, palette_type;
    public int[] palette;
    boolean colors_corrected;
    public ColorConfig(int mode, int color_density, int num_colors, int basecolor) {
        initColorConfig(mode, color_density, num_colors, basecolor);
    }
    private void initColorConfig(int mode, int color_density, int num_colors, int basecolor) {
        colors_corrected = false;
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        calcStep();
        initGradientPalette();
        setMode(mode);
    }
    public void initGradientPalette() {
        setPalette_type(Colors.PALETTE.CUSTOM);
        palette = new int[num_colors];
        if (step == 0) {
            initShadePalette();
            return;
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
        int r = (color >> 16) & 0xFF; int g = (color >> 8) & 0xFF; int b = (color) & 0xFF;
        int nr = (int) (r + (255 - r) * tint); int ng = (int) (g + (255 - g) * tint);
        int nb = (int) (b + (255 - b) * tint); return (nr << 16) | (ng << 8) | (nb);
    }
    public int getShade(int color, double shade) {
        int r = (color >> 16) & 0xFF; int g = (color >> 8) & 0xFF; int b = (color) & 0xFF;
        int nr = (int) (r * (1 - shade)); int ng = (int) (g * (1 - shade)); int nb = (int) (b * (1 - shade));
        return (nr << 16) | (ng << 8) | (nb);
    }
    private void calcStep() {
        setStep((0xfffff / (color_density << num_colors)));
    }
    public ColorConfig(int mode, int color_density, int num_colors, int basecolor, int step) {
        initColorConfig(mode, color_density, num_colors, basecolor, step);
    }
    private void initColorConfig(int mode, int color_density, int num_colors, int basecolor, int step) {
        colors_corrected = false;
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        setStep(step);
        initGradientPalette();
        setMode(mode);
    }
    public ColorConfig(int mode, int[] palette) {
        setPalette(palette, false);
        setMode(mode);
    }
    public void setPalette(int[] palette, boolean preserve) {
        if (!preserve) {
            this.palette = new int[palette.length];
            System.arraycopy(palette, 0, this.palette, 0, palette.length);
        } else {
            int[] tmpPalette = new int[this.palette.length];
            System.arraycopy(this.palette, 0, tmpPalette, 0, this.palette.length);
            this.palette = new int[num_colors];
            System.arraycopy(tmpPalette, 0, this.palette, 0, tmpPalette.length);
            System.arraycopy(palette, 0, this.palette, tmpPalette.length, this.palette.length - tmpPalette.length);
        }
    }
    public ColorConfig(int mode, int color_density, int num_colors) {
        initColorConfig(mode, num_colors);
        setColor_density(color_density);
    }
    private void initColorConfig(int mode, int num_colors) {
        colors_corrected = false;
        setNum_colors(num_colors);
        initRandomPalette(num_colors, false);
        setMode(mode);
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
        setPalette_type(Colors.PALETTE.RANDOM);
        initColorConfig(0, 0, 0x0, 0);
    }
    public ColorConfig(ColorConfig old) {
        initColorConfig(old.getMode(), old.getColor_density(), old.getNum_colors(), old.getBasecolor(), old.getStep());
        setPalette(old.getPalette(), false);
        colors_corrected = old.colors_corrected;
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
    public static int linearInterpolated(int fromcolor, int tocolor, int value, int maxvalue) {
        return linearInterpolated(fromcolor, tocolor, ((double) value) / maxvalue);
    }
    public static int linearInterpolated(int fromcolor, int tocolor, double bias) {
        return (int) (fromcolor * (1 - bias) + tocolor * (bias));
    }
    public int createIndex(double val, double min, double max) {
        return (int) Math.abs(((((val - min) / (max - min))) * color_density) % num_colors);
    }
    public int getColor(int index) {
        return palette[index];
    }
    public int splineInterpolated(int index, double bias) {
        if ((!colors_corrected) && num_colors < 4) {
            num_colors = 4;
            setBasecolor(basecolor);
            setColor_density(color_density);
            setMode(mode);
            setNum_colors(num_colors);
            setStep(step);
            if (palette_type == Colors.PALETTE.RANDOM) {
                initRandomPalette(num_colors, true);
            } else {
                initGradientPalette();
            }
            colors_corrected = true;
        }
        double h0 = 0.5 * ((bias * bias) * (bias - 1)),
                h1 = 0.5 * (bias * (1 + 4 * bias - 3 * (bias * bias))),
                h2 = 0.5 * (2 - 5 * (bias * bias) + 3 * (bias * bias * bias)),
                h3 = 0.5 * (bias * (2 * bias - (bias * bias) - 1));
        int i1 = ((index - 1) < 0) ? num_colors - 1 : index - 1, i2 = ((index - 2) < 0) ? num_colors - 2 : index - 2, i3 = ((index - 3) < 0) ? num_colors - 3 : index - 3;
        double color = (h0 * palette[index] + h1 * palette[i1] + h2 * palette[i2] + h3 * palette[i3]);
        color = (color < 0) ? -color : color;
        return (int) color;
    }
    public int getPalette_type() {
        return palette_type;
    }
    public void setPalette_type(int palette_type) {
        this.palette_type = palette_type;
    }
    public void colorsFromString(String[] colors) {
        mode = Integer.parseInt(colors[0]);
        if (colors[1].startsWith("0x")) {
            int[] palette = new int[colors.length];
            for (int i = 1; i < colors.length; i++) {
                palette[i] = Integer.parseInt(colors[i], 16);
            }
            setPalette(palette, false);
        } else {
            switch (colors.length) {
                case 2:
                    initColorConfig(mode, Integer.parseInt(colors[1]));
                    break;
                case 4:
                    initColorConfig(mode, Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[3], 16));
                    break;
                case 5:
                    initColorConfig(mode, Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[4], 16), Integer.parseInt(colors[3]));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported Input");
            }
        }
    }
    public boolean noCustomPalette() {
        return (palette_type == Colors.PALETTE.RANDOM) || (palette.length == 0) || (palette == null);
    }
}
