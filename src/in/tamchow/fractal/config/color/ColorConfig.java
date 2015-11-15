package in.tamchow.fractal.config.color;

/**
 * Holds colour configuration for  custom palettes
 */
public class ColorConfig {
    public int basecolor, step;
    public int[] palette;

    public ColorConfig(int basecolor, int step, int[] palette) {
        initColorConfig(basecolor, step, palette);
    }

    public ColorConfig(int basecolor, int step) {
        setBasecolor(basecolor);
        setStep(step);
    }

    public ColorConfig(int[] palette) {
        setPalette(palette);
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

    public void setPalette(int[] palette) {
        this.palette = new int[palette.length];

        System.arraycopy(palette, 0, this.palette, 0, palette.length);
    }

    private void initColorConfig(int basecolor, int step, int[] palette) {
        setBasecolor(basecolor);
        setPalette(palette);
        setStep(step);
    }

    public void colorsFromString(String[] colors) {
        if (colors[0].startsWith("0x")) {
            int[] palette = new int[colors.length];
            for (int i = 0; i < colors.length; i++) {
                palette[i] = Integer.parseInt(colors[i], 16);
            }
        } else {
            if (colors.length != 2) {
                throw new IllegalArgumentException("Undefined parameters");
            }
            setStep(Integer.parseInt(colors[0], 10));
            setBasecolor(Integer.parseInt(colors[1], 16));
        }
    }

    public boolean noCustomPalette() {
        return palette.length == 0 || palette == null;
    }
}
