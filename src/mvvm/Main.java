package mvvm;

import mvvm.model.attackontitan.AttackOnTitanRepository;
import mvvm.model.datasource.CharacterDataSource;
import mvvm.model.datasource.LocalCharacterDataSource;
import mvvm.model.datasource.RemoteCharacterDataSource;
import mvvm.model.rickandmorty.RickAndMortyRepository;
import mvvm.view.ImageLoader;
import mvvm.view.MainWindow;
import mvvm.viewmodel.AttackOnTitanFormatter;
import mvvm.viewmodel.CharacterBrowserViewModel;
import mvvm.viewmodel.RickAndMortyFormatter;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Punto de entrada. Aquí se "arma" la aplicación: se crean el Model, los ViewModels
 * y la View y se conectan entre sí. Ninguna otra clase crea sus propias dependencias.
 *
 * <p>Argumento opcional {@code --offline}: no usa internet y muestra los JSON de {@code resources/}.</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        boolean offline = Arrays.asList(args).contains("--offline");

        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        ExecutorService background = Executors.newVirtualThreadPerTaskExecutor();
        Executor ui = SwingUtilities::invokeLater;

        // ------------------------------------------------------------ Model
        RickAndMortyRepository rickAndMortyRepository = new RickAndMortyRepository(
                offline ? offlineSource() : new RemoteCharacterDataSource(http, RickAndMortyRepository.API_URL),
                new LocalCharacterDataSource(RickAndMortyRepository.LOCAL_FILE));
        AttackOnTitanRepository attackOnTitanRepository = new AttackOnTitanRepository(
                offline ? offlineSource() : new RemoteCharacterDataSource(http, AttackOnTitanRepository.API_URL),
                new LocalCharacterDataSource(AttackOnTitanRepository.LOCAL_FILE));

        // ------------------------------------------------------------ ViewModel
        var rickAndMorty = new CharacterBrowserViewModel<>("Rick and Morty",
                rickAndMortyRepository, new RickAndMortyFormatter(), background, ui);
        var attackOnTitan = new CharacterBrowserViewModel<>("Attack on Titan",
                attackOnTitanRepository, new AttackOnTitanFormatter(), background, ui);

        // ------------------------------------------------------------ View
        ImageLoader images = new ImageLoader(http, background);
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            new MainWindow(List.of(rickAndMorty, attackOnTitan), images).setVisible(true);
            rickAndMorty.loadInitial();
            attackOnTitan.loadInitial();
        });
    }

    /** Fuente que siempre falla, para forzar el uso del archivo local. */
    private static CharacterDataSource offlineSource() {
        return (page, query) -> {
            throw new IOException("modo --offline");
        };
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // Si falla se queda el look & feel por defecto de Java; no es grave.
        }
    }
}
