package mvvm.view;

import mvvm.viewmodel.CharacterBrowserViewModel;
import mvvm.viewmodel.CharacterItem;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Pestaña de una API. Es genérica: sirve igual para Rick and Morty y Attack on Titan.
 *
 * <p>Solo hace dos cosas, como manda MVVM:</p>
 * <ol>
 *     <li>{@link #bindViewModel()}: se suscribe a las propiedades del ViewModel y pinta lo que cambie.</li>
 *     <li>{@link #wireUserActions()}: traduce clics y teclas en comandos del ViewModel.</li>
 * </ol>
 * No contiene lógica de negocio ni sabe de HTTP o JSON.
 */
public final class CharacterBrowserPanel extends JPanel {

    private final CharacterBrowserViewModel<?> viewModel;

    private final DefaultListModel<CharacterItem> listModel = new DefaultListModel<>();
    private final JList<CharacterItem> list = new JList<>(listModel);
    private final JTextField searchField = new JTextField(24);
    private final JButton searchButton = new JButton("Buscar");
    private final JButton clearButton = new JButton("Limpiar");
    private final JButton reloadButton = new JButton("Recargar");
    private final JButton previousButton = new JButton("‹ Anterior");
    private final JButton nextButton = new JButton("Siguiente ›");
    private final JLabel pageLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar();
    private final CharacterDetailPanel detailPanel;

    /** Evita que los cambios hechos desde el ViewModel vuelvan a él como si fueran del usuario. */
    private boolean syncingFromViewModel;

    public CharacterBrowserPanel(CharacterBrowserViewModel<?> viewModel, ImageLoader images) {
        super(new BorderLayout());
        this.viewModel = viewModel;
        this.detailPanel = new CharacterDetailPanel(images);
        buildLayout(images);
        bindViewModel();
        wireUserActions();
    }

    private void buildLayout(ImageLoader images) {
        setBorder(new EmptyBorder(10, 10, 8, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBar.add(new JLabel("Buscar por nombre:"));
        searchBar.add(searchField);
        searchBar.add(searchButton);
        searchBar.add(clearButton);
        searchBar.add(reloadButton);
        add(searchBar, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new CharacterCellRenderer(images, list::repaint));
        list.setFixedCellHeight(CharacterCellRenderer.THUMB_SIZE + 14);
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setMinimumSize(new Dimension(280, 200));
        listScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, detailPanel);
        split.setDividerLocation(380);
        split.setResizeWeight(0);
        split.setContinuousLayout(true);
        split.setBorder(new EmptyBorder(8, 0, 8, 0));
        add(split, BorderLayout.CENTER);

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navigation.add(previousButton);
        navigation.add(pageLabel);
        navigation.add(nextButton);

        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setPreferredSize(new Dimension(110, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel status = new JPanel(new BorderLayout(10, 0));
        status.add(statusLabel, BorderLayout.CENTER);
        status.add(progress, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(16, 0));
        bottom.add(navigation, BorderLayout.WEST);
        bottom.add(status, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /** ViewModel → View. */
    private void bindViewModel() {
        viewModel.items.observe(items -> syncing(() -> {
            listModel.clear();
            listModel.addAll(items);
        }));
        viewModel.selectedIndex.observe(index -> syncing(() -> {
            if (index < 0) {
                list.clearSelection();
            } else {
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }));
        viewModel.selectedDetail.observe(detailPanel::show);
        viewModel.loading.observe(loading -> {
            progress.setVisible(loading);
            searchButton.setEnabled(!loading);
            reloadButton.setEnabled(!loading);
        });
        viewModel.pageLabel.observe(text -> pageLabel.setText(text.isEmpty() ? " " : text));
        viewModel.canGoPrevious.observe(previousButton::setEnabled);
        viewModel.canGoNext.observe(nextButton::setEnabled);
        viewModel.statusMessage.observe(text -> {
            statusLabel.setText(text.isEmpty() ? " " : text);
            statusLabel.setToolTipText(text.isEmpty() ? null : text);
        });
        viewModel.statusTone.observe(tone -> statusLabel.setForeground(Theme.tone(tone)));
    }

    /** View → ViewModel. */
    private void wireUserActions() {
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !syncingFromViewModel) {
                viewModel.select(list.getSelectedIndex());
            }
        });
        searchField.addActionListener(e -> viewModel.search(searchField.getText()));
        searchButton.addActionListener(e -> viewModel.search(searchField.getText()));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            viewModel.clearSearch();
        });
        reloadButton.addActionListener(e -> viewModel.reload());
        previousButton.addActionListener(e -> viewModel.previousPage());
        nextButton.addActionListener(e -> viewModel.nextPage());
    }

    private void syncing(Runnable update) {
        syncingFromViewModel = true;
        try {
            update.run();
        } finally {
            syncingFromViewModel = false;
        }
    }
}
