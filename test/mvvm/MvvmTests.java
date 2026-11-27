package mvvm;

import mvvm.core.json.JsonException;
import mvvm.core.json.JsonObject;
import mvvm.core.json.JsonParser;
import mvvm.model.attackontitan.AttackOnTitanCharacter;
import mvvm.model.attackontitan.AttackOnTitanMapper;
import mvvm.model.attackontitan.AttackOnTitanRepository;
import mvvm.model.common.ApiText;
import mvvm.model.common.DataOrigin;
import mvvm.model.common.Gender;
import mvvm.model.common.LifeStatus;
import mvvm.model.common.Page;
import mvvm.model.datasource.CharacterDataSource;
import mvvm.model.datasource.LocalCharacterDataSource;
import mvvm.model.repository.CharacterRepository;
import mvvm.model.repository.RepositoryException;
import mvvm.model.rickandmorty.RickAndMortyCharacter;
import mvvm.model.rickandmorty.RickAndMortyMapper;
import mvvm.model.rickandmorty.RickAndMortyRepository;
import mvvm.viewmodel.AttackOnTitanFormatter;
import mvvm.viewmodel.CharacterBrowserViewModel;
import mvvm.viewmodel.CharacterDetail;
import mvvm.viewmodel.CharacterDetail.DetailRow;
import mvvm.viewmodel.RickAndMortyFormatter;
import mvvm.viewmodel.StatusTone;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.Executor;

/**
 * Pruebas sin librerías externas: se ejecutan como un {@code main} normal.
 * Necesitan la carpeta {@code resources/} en el classpath (ver README).
 */
public final class MvvmTests {

    private static final Executor SAME_THREAD = Runnable::run;
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        run("JsonParser: objetos, arreglos, números, escapes y errores", MvvmTests::jsonParser);
        run("ApiText: notas de la wiki, 'unknown' e ids en URLs", MvvmTests::apiText);
        run("Enums: estados y géneros de ambas APIs", MvvmTests::enums);
        run("Rick and Morty: mapeo del JSON de la carpeta", MvvmTests::rickAndMortyFile);
        run("Attack on Titan: mapeo del JSON de la carpeta", MvvmTests::attackOnTitanFile);
        run("Attack on Titan: respuesta sin resultados (sin 'results')", MvvmTests::attackOnTitanEmpty);
        run("Repositorio: 404 de la API = página vacía", MvvmTests::repositoryNotFound);
        run("Repositorio: sin red usa el archivo local y filtra", MvvmTests::repositoryOfflineFallback);
        run("Formatters: textos listos para la vista", MvvmTests::formatters);
        run("ViewModel: carga, selección y paginación", MvvmTests::viewModelLoadsAndPaginates);
        run("ViewModel: un error conserva la lista anterior", MvvmTests::viewModelKeepsDataOnError);

        System.out.printf("%n%d pruebas OK, %d fallidas%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ core

    private static void jsonParser() {
        Object parsed = JsonParser.parse("{\"a\": [1, 2.5, -3e2], \"b\": {\"c\": null, \"d\": true}, \"e\": \"x\\\"y\\u00e9\\n\"}");
        Map<?, ?> root = (Map<?, ?>) parsed;
        equal(List.of(1L, 2.5, -300.0), root.get("a"), "arreglo de números");
        equal(true, ((Map<?, ?>) root.get("b")).get("d"), "booleano anidado");
        check(((Map<?, ?>) root.get("b")).containsKey("c"), "conserva claves con null");
        equal("x\"yé\n", root.get("e"), "escapes");

        JsonObject json = JsonObject.parse("{\"age\": 19, \"other\": \"unknown\"}");
        equal(Optional.of("19"), json.optScalarAsText("age"), "número como texto");
        equal(Optional.of("unknown"), json.optScalarAsText("other"), "texto como texto");
        equal(List.of(), json.strings("missing"), "arreglo ausente = vacío");

        expectThrows(JsonException.class, () -> JsonParser.parse("{\"a\": }"));
        expectThrows(JsonException.class, () -> JsonParser.parse("[1, 2"));
        expectThrows(JsonException.class, () -> JsonObject.parse("{\"id\": \"1\"}").integer("id"));
    }

    private static void apiText() {
        equal("Liberio", ApiText.clean("Liberio [2][3][4]"), "varias notas");
        equal("Ambiguous", ApiText.clean("Ambiguous[9]"), "nota pegada");
        equal("Wall Rose", ApiText.clean("Wall Rose [1]"), "nota con espacio");
        equal(Optional.empty(), ApiText.known("unknown"), "unknown");
        equal(Optional.empty(), ApiText.known(""), "vacío");
        equal(Optional.of("Earth (C-137)"), ApiText.known("Earth (C-137)"), "texto normal");
        equal(OptionalInt.of(28), ApiText.trailingId("https://rickandmortyapi.com/api/episode/28"), "id");
        equal(OptionalInt.of(3), ApiText.trailingId("https://api.attackontitanapi.com/episodes/3/"), "id con barra final");
        equal(OptionalInt.empty(), ApiText.trailingId(""), "url vacía");
    }

    private static void enums() {
        equal(LifeStatus.DEAD, LifeStatus.fromApi("Dead"), "R&M Dead");
        equal(LifeStatus.DEAD, LifeStatus.fromApi("Deceased"), "AoT Deceased");
        equal(LifeStatus.UNKNOWN, LifeStatus.fromApi("unknown"), "R&M unknown");
        equal(LifeStatus.UNKNOWN, LifeStatus.fromApi("Unknown"), "AoT Unknown");
        equal(LifeStatus.ALIVE, LifeStatus.fromApi("Alive"), "Alive");
        equal(Gender.AMBIGUOUS, Gender.fromApi("Ambiguous[9]"), "género con nota");
        equal(Gender.GENDERLESS, Gender.fromApi("Genderless"), "Genderless");
        equal(Gender.UNKNOWN, Gender.fromApi(null), "null");
    }

    // ------------------------------------------------------------------ model

    private static void rickAndMortyFile() throws IOException {
        String json = readLocal(RickAndMortyRepository.LOCAL_FILE);
        Page<RickAndMortyCharacter> page = RickAndMortyMapper.toPage(RickAndMortyMapper.parseResponse(json), 1, DataOrigin.LOCAL);

        equal(20, page.items().size(), "personajes en la página");
        equal(42, page.totalPages(), "total de páginas");
        equal(826, page.totalCount(), "total de personajes");
        check(page.hasNext() && !page.hasPrevious(), "navegación de la página 1");

        RickAndMortyCharacter rick = page.items().getFirst();
        equal("Rick Sanchez", rick.name(), "nombre");
        equal(LifeStatus.ALIVE, rick.status(), "estado");
        equal(Gender.MALE, rick.gender(), "género");
        equal(Optional.of("Human"), rick.species(), "especie");
        equal(Optional.empty(), rick.subtype(), "type \"\" → vacío");
        equal(Optional.of("Earth (C-137)"), rick.origin().name(), "origen");
        equal(OptionalInt.of(1), rick.origin().id(), "id del origen");
        equal(OptionalInt.of(3), rick.location().id(), "id de la ubicación");
        equal(51, rick.episodeIds().size(), "episodios");
        equal(1, rick.episodeIds().getFirst(), "primer episodio");
        equal(Instant.parse("2017-11-04T18:48:46.250Z"), rick.created(), "fecha");

        RickAndMortyCharacter morty = page.items().get(1);
        equal(Optional.empty(), morty.origin().name(), "origen unknown → vacío");
        equal(OptionalInt.empty(), morty.origin().id(), "origen sin URL → sin id");
        check(page.items().stream().anyMatch(c -> c.subtype().isPresent()), "hay personajes con subtipo");
    }

    private static void attackOnTitanFile() throws IOException {
        String json = readLocal(AttackOnTitanRepository.LOCAL_FILE);
        Page<AttackOnTitanCharacter> page = AttackOnTitanMapper.toPage(AttackOnTitanMapper.parseResponse(json), 1, DataOrigin.LOCAL);

        equal(20, page.items().size(), "personajes en la página");
        equal(11, page.totalPages(), "total de páginas (next_page/prev_page)");
        equal(201, page.totalCount(), "total de personajes");

        AttackOnTitanCharacter armin = page.items().getFirst();
        equal("Armin Arlelt", armin.name(), "nombre");
        equal(OptionalInt.of(19), armin.age(), "edad numérica");
        equal(List.of("Colossal Titan"), armin.aliases(), "alias");
        equal(List.of("Human", "Intelligent Titan"), armin.species(), "especies");
        equal("Scout Regiment", armin.groups().getFirst().name(), "grupo");
        equal(List.of("Special Operations Squad"), armin.groups().getFirst().subGroups(), "sub_groups");
        equal("Arlelt family", armin.relatives().getFirst().name(), "familia");
        equal(3, armin.relatives().getFirst().members().size(), "miembros de la familia");
        check(armin.imageUrl().isPresent(), "Armin tiene imagen");

        AttackOnTitanCharacter mikasa = page.items().get(1);
        equal("Mikasa Ackermann", mikasa.name(), "segundo personaje");
        check(mikasa.relatives().stream().anyMatch(f -> f.memberIds().contains(165)), "familiar como URL → id 165");
        check(page.items().stream()
                        .flatMap(c -> c.relatives().stream())
                        .flatMap(f -> f.members().stream())
                        .noneMatch(m -> m.startsWith("http")),
                "ningún nombre de familiar es una URL");

        check(page.items().stream().anyMatch(c -> c.imageUrl().isEmpty()), "hay un personaje sin 'img'");
        check(page.items().stream().anyMatch(c -> c.age().isEmpty()), "edades 'unknown' → vacío");
        check(page.items().stream().anyMatch(c -> c.status() == LifeStatus.DEAD), "'Deceased' → DEAD");
        check(page.items().stream().noneMatch(c -> c.height().map("unknown"::equalsIgnoreCase).orElse(false)),
                "ninguna altura queda como 'unknown'");
    }

    private static void attackOnTitanEmpty() {
        String json = "{\"info\":{\"count\":0,\"pages\":0,\"next_page\":null,\"prev_page\":null}}";
        Page<AttackOnTitanCharacter> page = AttackOnTitanMapper.toPage(AttackOnTitanMapper.parseResponse(json), 1, DataOrigin.REMOTE);
        check(page.isEmpty(), "sin personajes");
        check(!page.hasNext() && !page.hasPrevious(), "sin navegación");
    }

    private static void repositoryNotFound() throws RepositoryException {
        CharacterDataSource notFound = (page, query) -> Optional.empty();
        Page<RickAndMortyCharacter> page = new RickAndMortyRepository(notFound, null).findCharacters(1, "zzz");
        check(page.isEmpty(), "página vacía");
        equal(DataOrigin.REMOTE, page.origin(), "origen");
    }

    private static void repositoryOfflineFallback() throws RepositoryException {
        CharacterDataSource noNetwork = (page, query) -> {
            throw new IOException("sin red");
        };
        var repository = new RickAndMortyRepository(noNetwork, new LocalCharacterDataSource(RickAndMortyRepository.LOCAL_FILE));

        Page<RickAndMortyCharacter> all = repository.findCharacters(1, "");
        equal(DataOrigin.LOCAL, all.origin(), "origen local");
        equal(20, all.items().size(), "todos los del archivo");
        check(!all.hasNext(), "sin página siguiente cuando es local");

        Page<RickAndMortyCharacter> ricks = repository.findCharacters(1, "RICK");
        check(!ricks.isEmpty(), "encuentra Ricks");
        check(ricks.items().stream().allMatch(c -> c.name().toLowerCase().contains("rick")), "filtra por nombre sin importar mayúsculas");

        expectThrows(RepositoryException.class, () -> repository.findCharacters(2, ""));
        expectThrows(RepositoryException.class, () -> new RickAndMortyRepository(noNetwork, null).findCharacters(1, ""));
    }

    // ------------------------------------------------------------------ viewmodel

    private static void formatters() throws IOException {
        var rick = RickAndMortyMapper.toPage(RickAndMortyMapper.parseResponse(readLocal(RickAndMortyRepository.LOCAL_FILE)), 1, DataOrigin.LOCAL).items().getFirst();
        CharacterDetail rickDetail = new RickAndMortyFormatter().toDetail(rick);
        equal("Vivo", rickDetail.statusLabel(), "estado en español");
        equal(StatusTone.POSITIVE, rickDetail.tone(), "tono");
        equal("51 episodios (1–51)", row(rickDetail, "Episodios"), "rangos de episodios");
        equal("04/11/2017", row(rickDetail, "Alta en la API"), "fecha formateada");

        var armin = AttackOnTitanMapper.toPage(AttackOnTitanMapper.parseResponse(readLocal(AttackOnTitanRepository.LOCAL_FILE)), 1, DataOrigin.LOCAL).items().getFirst();
        CharacterDetail arminDetail = new AttackOnTitanFormatter().toDetail(armin);
        equal("19 años", row(arminDetail, "Edad"), "edad");
        equal("Scout Regiment (Special Operations Squad)", row(arminDetail, "Grupos"), "grupos");
        check(row(arminDetail, "Episodios").contains("–"), "episodios con huecos en rangos");
    }

    private static void viewModelLoadsAndPaginates() throws IOException {
        List<RickAndMortyCharacter> sample = RickAndMortyMapper.toPage(
                RickAndMortyMapper.parseResponse(readLocal(RickAndMortyRepository.LOCAL_FILE)), 1, DataOrigin.REMOTE).items();
        CharacterRepository<RickAndMortyCharacter> fake = (page, query) -> new Page<>(sample, page, 3, 60, DataOrigin.REMOTE);
        var vm = new CharacterBrowserViewModel<>("Prueba", fake, new RickAndMortyFormatter(), SAME_THREAD, SAME_THREAD);

        vm.loadInitial();
        equal(20, vm.items.get().size(), "elementos");
        equal(0, vm.selectedIndex.get(), "selecciona el primero");
        equal("Rick Sanchez", vm.selectedDetail.get().orElseThrow().name(), "detalle");
        equal("Página 1 de 3", vm.pageLabel.get(), "etiqueta de página");
        check(vm.canGoNext.get() && !vm.canGoPrevious.get(), "botones en la página 1");
        check(!vm.loading.get(), "terminó de cargar");

        vm.nextPage();
        equal("Página 2 de 3", vm.pageLabel.get(), "siguiente página");
        check(vm.canGoPrevious.get(), "puede volver");

        vm.select(4);
        equal(sample.get(4).name(), vm.selectedDetail.get().orElseThrow().name(), "selección manual");
        vm.select(99);
        equal(Optional.empty(), vm.selectedDetail.get(), "índice inválido limpia el detalle");
    }

    private static void viewModelKeepsDataOnError() throws IOException {
        List<AttackOnTitanCharacter> sample = AttackOnTitanMapper.toPage(
                AttackOnTitanMapper.parseResponse(readLocal(AttackOnTitanRepository.LOCAL_FILE)), 1, DataOrigin.LOCAL).items();
        boolean[] fail = {false};
        CharacterRepository<AttackOnTitanCharacter> flaky = (page, query) -> {
            if (fail[0]) {
                throw new RepositoryException("La API no responde", null);
            }
            return new Page<>(sample, 1, 1, sample.size(), DataOrigin.LOCAL);
        };
        var vm = new CharacterBrowserViewModel<>("Prueba", flaky, new AttackOnTitanFormatter(), SAME_THREAD, SAME_THREAD);

        vm.loadInitial();
        equal(StatusTone.WARNING, vm.statusTone.get(), "datos locales = aviso");
        check(vm.statusMessage.get().startsWith("Sin conexión"), "mensaje de datos locales");

        fail[0] = true;
        vm.reload();
        equal(20, vm.items.get().size(), "conserva la lista");
        equal(StatusTone.NEGATIVE, vm.statusTone.get(), "tono de error");
        equal("La API no responde", vm.statusMessage.get(), "mensaje de error");
        check(!vm.loading.get(), "no se queda cargando");
    }

    // ------------------------------------------------------------------ utilidades

    private static String readLocal(String resource) throws IOException {
        return new LocalCharacterDataSource(resource).fetchPage(1, "").orElseThrow();
    }

    private static String row(CharacterDetail detail, String label) {
        return detail.rows().stream()
                .filter(r -> r.label().equals(label))
                .map(DetailRow::value)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no existe la fila " + label));
    }

    private interface TestBody {
        void run() throws Exception;
    }

    private static void run(String name, TestBody body) {
        try {
            body.run();
            passed++;
            System.out.println("[OK]    " + name);
        } catch (Throwable e) {
            failed++;
            System.out.println("[FALLA] " + name + "\n        " + e);
        }
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError(what);
        }
    }

    private static void equal(Object expected, Object actual, String what) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(what + ": se esperaba <" + expected + "> pero fue <" + actual + ">");
        }
    }

    private static void expectThrows(Class<? extends Throwable> type, TestBody body) {
        try {
            body.run();
        } catch (Throwable e) {
            if (type.isInstance(e)) {
                return;
            }
            throw new AssertionError("se esperaba " + type.getSimpleName() + " pero fue " + e);
        }
        throw new AssertionError("se esperaba " + type.getSimpleName() + " y no se lanzó nada");
    }
}
