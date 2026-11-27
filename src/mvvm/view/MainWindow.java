package mvvm.view;

import mvvm.viewmodel.CharacterBrowserViewModel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Dimension;
import java.util.List;

/** Ventana principal: una pestaña por cada API. */
public final class MainWindow extends JFrame {

    public MainWindow(List<CharacterBrowserViewModel<?>> viewModels, ImageLoader images) {
        super("Explorador de personajes · MVVM");
        JTabbedPane tabs = new JTabbedPane();
        for (CharacterBrowserViewModel<?> viewModel : viewModels) {
            tabs.addTab(viewModel.title(), new CharacterBrowserPanel(viewModel, images));
        }
        setContentPane(tabs);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 560));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }
}
