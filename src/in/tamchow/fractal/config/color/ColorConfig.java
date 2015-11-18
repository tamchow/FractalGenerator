package in.tamchow.fractal.config.color;

/**
 * Holds colour configuration for  custom palettes
 */
public class ColorConfig {
    public int basecolor, step, color_density, num_colors;
    public int[] palette;

    public ColorConfig(int color_density, int num_colors, int basecolor) {
        initColorConfig(color_density, num_colors, basecolor);
    }

    public ColorConfig(int[] palette) {
        setPalette(palette, false);
    }

    public ColorConfig() {
        palette = null;
        initColorConfig(0, 0, 0x0, 0);
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

    public void initRandomPalette(int num_colors, boolean preserve) {
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

    public void initGradientPalette() {
        palette = new int[num_colors];
        int baseidx = num_colors / 2;
        for (int i = 0; i < baseidx; i++) {
            palette[i] = ((basecolor - step) < 0) ? Math.abs(basecolor - step) : basecolor - step;
        }
        for (int i = baseidx; i < num_colors; i++) {
            palette[i] = basecolor + step;
        }
    }

    public void initGrayScalePalette(int max) {
        palette = new int[max];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = i << 16 | i << 8 | i;
        }
    }

    private void initColorConfig(int color_density, int num_colors, int basecolor, int step) {
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        setStep(step);
        initGradientPalette();
    }

    private void initColorConfig(int color_density, int num_colors, int basecolor) {
        setColor_density(color_density);
        setNum_colors(num_colors);
        setBasecolor(basecolor);
        calcStep();
        initGradientPalette();
    }

    private void initColorConfig(int num_colors) {
        initRandomPalette(num_colors, false);
    }

    private void calcStep() {
        setStep((0xfffff / (color_density << num_colors)));
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

    public void colorsFromString(String[] colors) {
        if (colors[0].startsWith("0x")) {
            int[] palette = new int[colors.length];
            for (int i = 0; i < colors.length; i++) {
                palette[i] = Integer.parseInt(colors[i], 16);
            }
            setPalette(palette, false);
        } else {
            switch (colors.length) {
                case 1:
                    initColorConfig(Integer.parseInt(colors[0]));
                    break;
                case 3:
                    initColorConfig(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2], 16));
                    break;
                case 4:
                    initColorConfig(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[3], 16), Integer.parseInt(colors[2]));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupoorted Input");
            }
        }
    }

    public boolean noCustomPalette() {
        return palette.length == 0 || palette == null;
    }
}
