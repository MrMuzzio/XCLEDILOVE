package xc.LEDILove.Bean;

import java.io.Serializable;

/**
 * Created by xcgd on 2018/3/26.
 */

public class TextBean implements Serializable {
    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public int getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(int backdrop) {
        this.backdrop = backdrop;
    }

    public int getFont() {
        return font;
    }

    public void setFont(int font) {
        this.font = font;
    }

    char character;
    int backdrop;
    int font;
}
