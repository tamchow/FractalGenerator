package in.tamchow.fractal.color;
/**
 * Holds information about an HSL color
 */
public class HSL {
    double hue, saturation, lightness;
    public HSL(double hue, double saturation, double lightness) {
        setHue(hue); setSaturation(saturation); setLightness(lightness);
    }
    public static HSL fromRGB(int color) {
        int ri = (color >> 16) & 0xFF; int gi = (color >> 8) & 0xFF; int bi = (color) & 0xFF; double r = ri / 255;
        double g = gi / 255; double b = bi / 255; double max = (r > g && r > b) ? r : (g > b) ? g : b;
        double min = (r < g && r < b) ? r : (g < b) ? g : b; double h, s, l; h = s = l = (max + min) / 2;
        if (max == min) h = s = 0;
        else {
            double d = max - min; s = (l > 0.5) ? d / (2 - max - min) : d / (max + min);
            if (r > g && r > b) h = (g - b) / d + (g < b ? 6 : 0);
            else if (g > b) h = (b - r) / d + 2;
            else h = (r - g) / d + 4; h /= 6;
        } return new HSL(h, s, l);
    }
    public double getHue() {return hue;}
    public void setHue(double hue) {this.hue = hue;}
    public double getSaturation() {return saturation;}
    public void setSaturation(double saturation) {this.saturation = saturation;}
    public double getLightness() {return lightness;}
    public void setLightness(double lightness) {this.lightness = lightness;}
    public int toRGB() {
        double r, g, b; if (saturation == 0.0) r = g = b = lightness;
        else {
            double q = lightness < 0.5 ? lightness * (1 + saturation) : lightness + saturation - lightness * saturation;
            double p = 2 * lightness - q; r = hueToRGB(p, q, hue + 1 / 3); g = hueToRGB(p, q, hue);
            b = hueToRGB(p, q, hue - 1 / 3);
        } int ri = (int) r * 255; int gi = (int) g * 255; int bi = (int) b * 255; return ri << 16 | gi << 8 | bi;
    }
    private double hueToRGB(double p, double q, double t) {
        if (t < 0.0f) t += 1.0f; if (t > 1.0f) t -= 1.0f; if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f) return q; if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f; return p;
    }
}
