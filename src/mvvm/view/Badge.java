package mvvm.view;

import mvvm.viewmodel.StatusTone;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Etiqueta redondeada para el estado del personaje ("Vivo", "Muerto"…). */
final class Badge extends JLabel {

    private Color fill = Theme.NEUTRAL;

    Badge() {
        setOpaque(false);
        setForeground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(new EmptyBorder(2, 9, 2, 9));
        setFont(getFont().deriveFont(Font.BOLD, getFont().getSize2D() - 1f));
    }

    void setTone(StatusTone tone) {
        fill = Theme.tone(tone);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
