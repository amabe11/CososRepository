package mvvm.view;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Imagen de reemplazo con las iniciales, mientras carga la foto o si no hay. */
final class InitialsIcon implements Icon {

    private final String initials;
    private final int width;
    private final int height;

    InitialsIcon(String name, int width, int height) {
        this.initials = initialsOf(name);
        this.width = width;
        this.height = height;
    }

    private static String initialsOf(String name) {
        StringBuilder sb = new StringBuilder();
        for (String word : name.strip().split("\\s+")) {
            if (!word.isEmpty() && Character.isLetterOrDigit(word.charAt(0))) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.isEmpty() ? "?" : sb.toString();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Theme.PLACEHOLDER);
        int arc = Math.min(width, height) / 4;
        g2.fillRoundRect(x, y, width, height, arc, arc);
        g2.setColor(Theme.PLACEHOLDER_TEXT);
        g2.setFont(c.getFont().deriveFont(Font.BOLD, Math.min(width, height) * 0.36f));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(initials)) / 2;
        int ty = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initials, tx, ty);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
