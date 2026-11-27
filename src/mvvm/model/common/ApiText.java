package mvvm.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * Utilidades de limpieza para los textos que llegan de las APIs.
 *
 * <p>Problemas reales encontrados al analizar los JSON:</p>
 * <ul>
 *     <li>Valores desconocidos escritos como {@code "unknown"}, {@code "Unknown"} o {@code ""}.</li>
 *     <li>Notas al pie de la wiki dentro del texto: {@code "Ambiguous[9]"}, {@code "Liberio [2][3][4]"}.</li>
 *     <li>Relaciones como URLs ({@code ".../episode/28"}) en lugar de ids.</li>
 * </ul>
 */
public final class ApiText {

    private static final Pattern FOOTNOTES = Pattern.compile("\\s*\\[\\d+]");

    private ApiText() {
    }

    /** Quita notas al pie y espacios sobrantes. {@code null} se convierte en {@code ""}. */
    public static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        return FOOTNOTES.matcher(raw).replaceAll("").strip();
    }

    /** Texto limpio, o vacío si la API dice que es desconocido. */
    public static Optional<String> known(String raw) {
        String text = clean(raw);
        if (text.isEmpty() || text.equalsIgnoreCase("unknown")) {
            return Optional.empty();
        }
        return Optional.of(text);
    }

    /** Aplica {@link #known} a cada elemento y descarta los desconocidos. */
    public static List<String> knownAll(List<String> raws) {
        List<String> result = new ArrayList<>();
        for (String raw : raws) {
            known(raw).ifPresent(result::add);
        }
        return List.copyOf(result);
    }

    /** URL sin espacios, o vacío si viene en blanco. No toca el contenido. */
    public static Optional<String> url(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(raw.strip());
    }

    /** Id final de una URL REST: {@code "https://.../episode/28"} → {@code 28}. */
    public static OptionalInt trailingId(String url) {
        if (url == null || url.isBlank()) {
            return OptionalInt.empty();
        }
        String path = url.strip();
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String last = path.substring(path.lastIndexOf('/') + 1);
        try {
            return OptionalInt.of(Integer.parseInt(last));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /** Aplica {@link #trailingId} a cada URL y descarta las que no tienen id. */
    public static List<Integer> trailingIds(List<String> urls) {
        List<Integer> ids = new ArrayList<>();
        for (String url : urls) {
            trailingId(url).ifPresent(ids::add);
        }
        return List.copyOf(ids);
    }
}
