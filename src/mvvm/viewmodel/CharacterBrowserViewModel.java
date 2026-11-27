package mvvm.viewmodel;

import mvvm.core.binding.ObservableProperty;
import mvvm.model.common.DataOrigin;
import mvvm.model.common.FictionalCharacter;
import mvvm.model.common.Page;
import mvvm.model.repository.CharacterRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * ViewModel del explorador de personajes. Hay una instancia por API.
 *
 * <p><b>Estado</b>: propiedades observables públicas que la View enlaza.<br>
 * <b>Comandos</b>: métodos públicos que la View llama ante acciones del usuario.</p>
 *
 * <p>No importa nada de Swing: el hilo de la interfaz llega como un {@link Executor}
 * ({@code SwingUtilities::invokeLater} en la app, {@code Runnable::run} en las pruebas).
 * Los comandos deben llamarse desde ese hilo y las propiedades solo cambian en él.</p>
 */
public final class CharacterBrowserViewModel<T extends FictionalCharacter> {

    // ------------------------------------------------------------------ estado enlazable
    public final ObservableProperty<List<CharacterItem>> items = new ObservableProperty<>(List.of());
    public final ObservableProperty<Integer> selectedIndex = new ObservableProperty<>(-1);
    public final ObservableProperty<Optional<CharacterDetail>> selectedDetail = new ObservableProperty<>(Optional.empty());
    public final ObservableProperty<Boolean> loading = new ObservableProperty<>(false);
    public final ObservableProperty<String> pageLabel = new ObservableProperty<>("");
    public final ObservableProperty<Boolean> canGoPrevious = new ObservableProperty<>(false);
    public final ObservableProperty<Boolean> canGoNext = new ObservableProperty<>(false);
    public final ObservableProperty<String> statusMessage = new ObservableProperty<>("");
    public final ObservableProperty<StatusTone> statusTone = new ObservableProperty<>(StatusTone.NEUTRAL);

    private final String title;
    private final CharacterRepository<T> repository;
    private final CharacterFormatter<T> formatter;
    private final Executor backgroundExecutor;
    private final Executor uiExecutor;

    private List<T> characters = List.of();
    private Page<T> currentPage;
    private String currentQuery = "";
    private int lastRequestId;

    public CharacterBrowserViewModel(String title,
                                     CharacterRepository<T> repository,
                                     CharacterFormatter<T> formatter,
                                     Executor backgroundExecutor,
                                     Executor uiExecutor) {
        this.title = Objects.requireNonNull(title, "title");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        this.backgroundExecutor = Objects.requireNonNull(backgroundExecutor, "backgroundExecutor");
        this.uiExecutor = Objects.requireNonNull(uiExecutor, "uiExecutor");
    }

    public String title() {
        return title;
    }

    // ------------------------------------------------------------------ comandos

    public void loadInitial() {
        requestPage(1, currentQuery);
    }

    public void reload() {
        requestPage(currentPage == null ? 1 : currentPage.number(), currentQuery);
    }

    public void search(String text) {
        requestPage(1, text == null ? "" : text.strip());
    }

    public void clearSearch() {
        search("");
    }

    public void nextPage() {
        if (canGoNext.get()) {
            requestPage(currentPage.number() + 1, currentQuery);
        }
    }

    public void previousPage() {
        if (canGoPrevious.get()) {
            requestPage(currentPage.number() - 1, currentQuery);
        }
    }

    public void select(int index) {
        int valid = index >= 0 && index < characters.size() ? index : -1;
        selectedIndex.set(valid);
        selectedDetail.set(valid < 0 ? Optional.empty() : Optional.of(formatter.toDetail(characters.get(valid))));
    }

    // ------------------------------------------------------------------ carga asíncrona

    private void requestPage(int page, String query) {
        int requestId = ++lastRequestId;
        loading.set(true);
        refreshNavigation();
        showStatus(query.isEmpty() ? "Cargando página " + page + "…" : "Buscando «" + query + "»…", StatusTone.NEUTRAL);

        backgroundExecutor.execute(() -> {
            try {
                Page<T> result = repository.findCharacters(page, query);
                uiExecutor.execute(() -> onPageLoaded(requestId, query, result));
            } catch (Exception e) {
                uiExecutor.execute(() -> onPageFailed(requestId, e));
            }
        });
    }

    private void onPageLoaded(int requestId, String query, Page<T> page) {
        if (requestId != lastRequestId) {
            return; // llegó tarde: el usuario ya pidió otra página
        }
        currentPage = page;
        currentQuery = query;

        select(-1);
        characters = page.items();
        items.set(characters.stream().map(formatter::toItem).toList());
        select(characters.isEmpty() ? -1 : 0);

        loading.set(false);
        pageLabel.set(page.totalPages() == 0 ? "Sin resultados" : "Página " + page.number() + " de " + page.totalPages());
        refreshNavigation();
        showStatus(describe(page, query), page.origin() == DataOrigin.LOCAL ? StatusTone.WARNING : StatusTone.NEUTRAL);
    }

    private void onPageFailed(int requestId, Exception error) {
        if (requestId != lastRequestId) {
            return;
        }
        loading.set(false);
        refreshNavigation();
        String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
        showStatus(message, StatusTone.NEGATIVE);
    }

    private void refreshNavigation() {
        boolean idle = !loading.get();
        canGoPrevious.set(idle && currentPage != null && currentPage.hasPrevious());
        canGoNext.set(idle && currentPage != null && currentPage.hasNext());
    }

    private void showStatus(String message, StatusTone tone) {
        statusMessage.set(message);
        statusTone.set(tone);
    }

    private static String describe(Page<?> page, String query) {
        if (page.isEmpty()) {
            return query.isEmpty() ? "La API no devolvió personajes." : "No hay personajes que coincidan con «" + query + "».";
        }
        String total = page.totalCount() + (page.totalCount() == 1 ? " personaje" : " personajes");
        String scope = query.isEmpty() ? total + " en total" : total + " para «" + query + "»";
        return page.origin() == DataOrigin.LOCAL
                ? "Sin conexión: mostrando el archivo local · " + scope
                : scope + " · API en línea";
    }
}
