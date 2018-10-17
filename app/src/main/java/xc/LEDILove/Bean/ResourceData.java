package xc.LEDILove.Bean;

import java.io.File;

/**
 * Created by xcgd on 2018/5/31.
 */

public class ResourceData {
    private File Drawable;
    private String text;

    @Override
    public String toString() {
        return "ResourceData{" +
                "Drawable=" + Drawable +
                ", text='" + text + '\'' +
                '}';
    }

    public File getDrawable() {
        return Drawable;
    }

    public void setDrawable(File drawable) {
        Drawable = drawable;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
