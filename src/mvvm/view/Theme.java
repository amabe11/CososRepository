package mvvm.view;

import mvvm.viewmodel.StatusTone;

import java.awt.Color;
import java.awt.Font;

/** Colores y tipografía de la interfaz. */
final class Theme {

    static final Color POSITIVE = new Color(0x2E7D32);
    static final Color NEGATIVE = new Color(0xC62828);
    static final Color WARNING = new Color(0xA15C00);
    static final Color NEUTRAL = new Color(0x6B7280);
    static final Color MUTED_TEXT = new Color(0x6B7280);
    static final Color PLACEHOLDER = new Color(0xE5E7EB);
    static final Color PLACEHOLDER_TEXT = new Color(0x4B5563);

    private Theme() {
    }

    static Color tone(StatusTone tone) {
        return switch (tone) {
            case POSITIVE -> POSITIVE;
            case NEGATIVE -> NEGATIVE;
            case WARNING -> WARNING;
            case NEUTRAL -> NEUTRAL;
        };
    }

    static Font derive(Font base, int style, float size) {
        return base.deriveFont(style, size);
    }

    static String hex(Color color) {
        return String.format("#%06x", color.getRGB() & 0xFFFFFF);
    }
}
