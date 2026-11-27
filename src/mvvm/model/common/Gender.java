package mvvm.model.common;

import java.util.Locale;

/**
 * Género normalizado.
 *
 * <ul>
 *     <li>Rick and Morty: {@code "Male"}, {@code "Female"}, {@code "Genderless"}, {@code "unknown"}</li>
 *     <li>Attack on Titan: {@code "Male"}, {@code "Female"}, {@code "unknown"} y
 *         {@code "Ambiguous[9]"} (con una nota al pie copiada de la wiki)</li>
 * </ul>
 */
public enum Gender {
    MALE,
    FEMALE,
    GENDERLESS,
    AMBIGUOUS,
    UNKNOWN;

    public static Gender fromApi(String raw) {
        return switch (ApiText.clean(raw).toLowerCase(Locale.ROOT)) {
            case "male" -> MALE;
            case "female" -> FEMALE;
            case "genderless" -> GENDERLESS;
            case "ambiguous" -> AMBIGUOUS;
            default -> UNKNOWN;
        };
    }
}
