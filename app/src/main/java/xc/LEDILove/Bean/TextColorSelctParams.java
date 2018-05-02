package xc.LEDILove.Bean;

/**
 * Created by xcgd on 2018/3/26.
 */

public class TextColorSelctParams {
    @Override
    public String toString() {
        return "TextColorSelctParams{" +
                "color_backdrop=" + color_backdrop +
                ", color_font=" + color_font +
                '}';
    }

    int color_backdrop;
    int color_font;

    public int getColor_backdrop() {
        return color_backdrop;
    }

    public void setColor_backdrop(int color_backdrop) {
        this.color_backdrop = color_backdrop;
    }

    public int getColor_font() {
        return color_font;
    }

    public void setColor_font(int color_font) {
        this.color_font = color_font;
    }
}
