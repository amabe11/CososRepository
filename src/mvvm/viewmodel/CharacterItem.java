package mvvm.viewmodel;

import java.util.Optional;

/**
 * Una fila de la lista, ya formateada para mostrar.
 *
 * @param name        nombre del personaje
 * @param subtitle    línea secundaria ("Human · Masculino")
 * @param statusLabel estado en texto ("Vivo")
 * @param tone        color sugerido para el estado
 * @param imageUrl    miniatura; vacío si no hay
 */
public record CharacterItem(String name, String subtitle, String statusLabel, StatusTone tone, Optional<String> imageUrl) {
}
