package in.tamchow.fractal.color;
import java.io.Serializable;
/**
 * Holds information about an HSL color
 */
public class HSL implements Serializable {
    double hue, saturation, lightness;
    public HSL(double hue, double saturation, double lightness) {
        setHue(hue); setSaturation(saturation); setLightness(lightness);
    }
    public static HSL fromString(String hsl) {
        String[] parts = hsl.split(",");
        return new HSL(hueFromAngle(Double.valueOf(parts[0])), Double.valueOf(parts[1]), Double.valueOf(parts[2]));
    }
    public static double hueFromAngle(double radianMeasure) {
        return radianMeasure / (2 * Math.PI);
    }
    public static HSL fromRGB(int color) {
        int ri = Color_Utils_Config.separateARGB(color, Colors.RGBCOMPONENTS.RED),
                gi = Color_Utils_Config.separateARGB(color, Colors.RGBCOMPONENTS.GREEN),
                bi = Color_Utils_Config.separateARGB(color, Colors.RGBCOMPONENTS.BLUE),
                max = (ri > gi && ri > bi) ? ri : (gi > bi) ? gi : bi,
                min = (ri < gi && ri < bi) ? ri : (gi < bi) ? gi : bi, c = max - min;
        double r = ri / 255.0, g = gi / 255.0, b = bi / 255.0, h, s, l = 0.5 * (max + min);
        if (c == 0) {h = 0; s = 0;} else {
            s = c / (1.0 - Math.abs(2 * l - 1));
            if (max == ri) {h = ((gi - bi) / c) % 6;} else if (max == gi) {h = ((bi - ri) / c) + 2;} else {
                h = ((ri - gi) / c) + 4;
            }
        } h *= (1.0 / 6.0); return new HSL(h, s, l);
    }
    public static double angleFromHue(double hue) {return hue * 2 * Math.PI;}
    @Override
    public boolean equals(Object other) {
        if (other instanceof HSL) {
            HSL that = (HSL) other;
            if (that.getHue() == getHue() && that.getLightness() == getLightness() && that.getSaturation() == getSaturation()) {
                return true;
            }
        } return false;
    }
    @Override
    public String toString() {return hue + "," + saturation + "," + lightness;}
    public double getHue() {return hue;}
    public void setHue(double hue) {
        if (hue < 0) {hue += 1; setHue(hue);} if (hue > 1) {
            hue -= 1; setHue(hue);
        } this.hue = hue;
    }
    public double getSaturation() {return saturation;}
    public void setSaturation(double saturation) {
        if (saturation < 0) {saturation += 1; setSaturation(saturation);}
        if (saturation > 1) {saturation -= 1; setSaturation(saturation);} this.saturation = saturation;
    }
    public double getLightness() {return lightness;}
    public void setLightness(double lightness) {
        if (lightness < 0) {lightness += 1; setLightness(lightness);}
        if (lightness > 1) {lightness -= 1; setLightness(lightness);} this.lightness = lightness;
    }
    public int toRGB() {
        double chroma = (1 - Math.abs(2 * lightness - 1)) * saturation, h = hue * 6, x = chroma * (1 - Math.abs((((int) h) % 2) - 1));
        int m = Math.round((float) (lightness - 0.5 * chroma)) * 255, r = m, g = m, b = m; if (h >= 0 && h < 1) {
            r += Math.round((float) chroma * 255); g += Math.round((float) x * 255); b += 0;
        } else if (h >= 1 && h < 2) {
            g += Math.round((float) chroma * 255); r += Math.round((float) x * 255); b += 0;
        } else if (h >= 2 && h < 3) {
            g += Math.round((float) chroma * 255); b += Math.round((float) x * 255); r += 0;
        } else if (h >= 3 && h < 4) {
            b += Math.round((float) chroma * 255); g += Math.round((float) x * 255); r += 0;
        } else if (h >= 4 && h < 5) {
            b += Math.round((float) chroma * 255); r += Math.round((float) x * 255); g += 0;
        } else if (h >= 5 && h < 6) {
            r += Math.round((float) chroma * 255); b += Math.round((float) x * 255); g += 0;
        } return Color_Utils_Config.toRGB(r, g, b);
    }
}