package mvvm.view;

import mvvm.viewmodel.CharacterItem;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

/** Dibuja cada fila de la lista: miniatura, nombre, subtítulo y estado. */
final class CharacterCellRenderer extends JPanel implements ListCellRenderer<CharacterItem> {

    static final int THUMB_SIZE = 44;

    private final ImageLoader images;
    private final Runnable repaintList;
    private final JLabel thumbnail = new JLabel();
    private final JLabel name = new JLabel();
    private final JLabel subtitle = new JLabel();
    private final Badge status = new Badge();

    CharacterCellRenderer(ImageLoader images, Runnable repaintList) {
        super(new BorderLayout(10, 0));
        this.images = images;
        this.repaintList = repaintList;
        setBorder(new EmptyBorder(6, 8, 6, 10));

        thumbnail.setPreferredSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
        name.setFont(Theme.derive(name.getFont(), Font.BOLD, name.getFont().getSize2D() + 1f));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);
        text.add(name);
        text.add(subtitle);

        JPanel badgeHolder = new JPanel(new GridBagLayout());
        badgeHolder.setOpaque(false);
        badgeHolder.add(status);

        add(thumbnail, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        add(badgeHolder, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends CharacterItem> list, CharacterItem item,
                                                  int index, boolean selected, boolean focused) {
        setBackground(selected ? list.getSelectionBackground() : list.getBackground());
        name.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
        subtitle.setForeground(selected ? list.getSelectionForeground() : Theme.MUTED_TEXT);

        name.setText(item.name());
        subtitle.setText(item.subtitle());
        status.setText(item.statusLabel());
        status.setTone(item.tone());
        thumbnail.setIcon(thumbnailFor(item));
        return this;
    }

    private Icon thumbnailFor(CharacterItem item) {
        if (item.imageUrl().isPresent()) {
            String url = item.imageUrl().get();
            Icon cached = images.cachedIcon(url, THUMB_SIZE, THUMB_SIZE, true);
            if (cached != null) {
                return cached;
            }
            images.loadIcon(url, THUMB_SIZE, THUMB_SIZE, true, icon -> repaintList.run(), () -> { });
        }
        return new InitialsIcon(item.name(), THUMB_SIZE, THUMB_SIZE);
    }
}
