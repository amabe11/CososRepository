package mvvm.view;

import mvvm.viewmodel.CharacterDetail;
import mvvm.viewmodel.CharacterDetail.DetailRow;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Optional;

/** Ficha del personaje seleccionado: imagen, nombre, estado y tabla de datos. */
final class CharacterDetailPanel extends JPanel {

    private static final int IMAGE_WIDTH = 240;
    private static final int IMAGE_HEIGHT = 320;
    private static final String EMPTY_CARD = "empty";
    private static final String DETAIL_CARD = "detail";

    private final ImageLoader images;
    private final CardLayout cards = new CardLayout();
    private final JLabel image = new JLabel();
    private final JLabel name = new JLabel();
    private final JLabel subtitle = new JLabel();
    private final Badge status = new Badge();
    private final JEditorPane table = new JEditorPane();
    private final JScrollPane tableScroll = new JScrollPane(table);
    private String shownImageUrl;

    CharacterDetailPanel(ImageLoader images) {
        this.images = images;
        setLayout(cards);
        add(buildEmptyState(), EMPTY_CARD);
        add(buildDetail(), DETAIL_CARD);
        cards.show(this, EMPTY_CARD);
    }

    /** Punto de enlace con {@code viewModel.selectedDetail}. */
    void show(Optional<CharacterDetail> detail) {
        if (detail.isEmpty()) {
            shownImageUrl = null;
            cards.show(this, EMPTY_CARD);
            return;
        }
        CharacterDetail d = detail.get();
        name.setText(d.name());
        subtitle.setText(d.subtitle());
        status.setText(d.statusLabel());
        status.setTone(d.tone());
        table.setText(toHtml(d.rows()));
        table.setCaretPosition(0);
        showImage(d);
        cards.show(this, DETAIL_CARD);
    }

    private void showImage(CharacterDetail d) {
        shownImageUrl = d.imageUrl().orElse(null);
        image.setIcon(new InitialsIcon(d.name(), IMAGE_WIDTH, IMAGE_WIDTH));
        if (shownImageUrl == null) {
            image.setText("Sin imagen en la API");
            return;
        }
        String requested = shownImageUrl;
        Icon cached = images.cachedIcon(requested, IMAGE_WIDTH, IMAGE_HEIGHT, false);
        if (cached != null) {
            image.setText(null);
            image.setIcon(cached);
            return;
        }
        if (images.hasFailed(requested, IMAGE_WIDTH, IMAGE_HEIGHT, false)) {
            image.setText("Imagen no disponible");
            return;
        }
        image.setText("Cargando imagen…");
        // Al volver, el usuario pudo haber elegido otro personaje: solo se pinta si sigue siendo el mismo.
        images.loadIcon(requested, IMAGE_WIDTH, IMAGE_HEIGHT, false,
                icon -> {
                    if (requested.equals(shownImageUrl)) {
                        image.setText(null);
                        image.setIcon(icon);
                    }
                },
                () -> {
                    if (requested.equals(shownImageUrl)) {
                        image.setText("Imagen no disponible");
                    }
                });
    }

    private JComponent buildEmptyState() {
        JLabel label = new JLabel("Selecciona un personaje de la lista", SwingConstants.CENTER);
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private JComponent buildDetail() {
        image.setVerticalAlignment(SwingConstants.TOP);
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setVerticalTextPosition(SwingConstants.BOTTOM);
        image.setHorizontalTextPosition(SwingConstants.CENTER);
        image.setIconTextGap(8);
        image.setForeground(Theme.MUTED_TEXT);
        image.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));

        Font base = UIManager.getFont("Label.font");
        name.setFont(Theme.derive(base, Font.BOLD, base.getSize2D() + 10f));
        subtitle.setFont(Theme.derive(base, Font.PLAIN, base.getSize2D() + 1f));
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        for (JComponent c : List.of(name, subtitle, status)) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        // Margen para que el texto no se recorte con escalados de pantalla no enteros.
        name.setBorder(new EmptyBorder(0, 0, 0, 6));
        subtitle.setBorder(new EmptyBorder(0, 0, 0, 6));
        header.add(name);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(10));
        header.add(status);

        table.setContentType("text/html");
        table.setEditable(false);
        table.setOpaque(false);
        table.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        table.setFont(Theme.derive(base, Font.PLAIN, base.getSize2D() + 1f));
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel info = new JPanel(new BorderLayout());
        info.setBorder(new EmptyBorder(0, 18, 0, 0));
        info.add(header, BorderLayout.NORTH);
        info.add(tableScroll, BorderLayout.CENTER);

        JPanel imageHolder = new JPanel(new BorderLayout());
        imageHolder.add(image, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(14, 16, 14, 14));
        panel.add(imageHolder, BorderLayout.WEST);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    private static String toHtml(List<DetailRow> rows) {
        StringBuilder html = new StringBuilder("<html><body style='margin:0'><table cellspacing='0' cellpadding='5'>");
        String labelColor = Theme.hex(Theme.MUTED_TEXT);
        for (DetailRow row : rows) {
            html.append("<tr><td valign='top' nowrap style='color:").append(labelColor).append("'><b>")
                    .append(escape(row.label()))
                    .append("</b></td><td valign='top'>")
                    .append(escape(row.value()).replace("\n", "<br>"))
                    .append("</td></tr>");
        }
        return html.append("</table></body></html>").toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
