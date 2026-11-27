package mvvm.model.common;

import java.util.Locale;

/**
 * Estado vital normalizado.
 *
 * <p>Cada API lo escribe distinto:</p>
 * <ul>
 *     <li>Rick and Morty: {@code "Alive"}, {@code "Dead"}, {@code "unknown"}</li>
 *     <li>Attack on Titan: {@code "Alive"}, {@code "Deceased"}, {@code "Unknown"}</li>
 * </ul>
 */
public enum LifeStatus {
    ALIVE,
    DEAD,
    UNKNOWN;

    public static LifeStatus fromApi(String raw) {
        return switch (ApiText.clean(raw).toLowerCase(Locale.ROOT)) {
            case "alive" -> ALIVE;
            case "dead", "deceased" -> DEAD;
            default -> UNKNOWN;
        };
    }
}
