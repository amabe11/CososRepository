package mvvm.viewmodel;

import java.util.List;
import java.util.Optional;

/**
 * Ficha completa del personaje seleccionado, ya formateada para mostrar.
 *
 * @param rows pares etiqueta/valor; cada API decide cuáles muestra
 */
public record CharacterDetail(
        String name,
        String subtitle,
        String statusLabel,
        StatusTone tone,
        Optional<String> imageUrl,
        List<DetailRow> rows) {

    public CharacterDetail {
        rows = List.copyOf(rows);
    }

    /** Un dato de la ficha: "Especie" → "Human". */
    public record DetailRow(String label, String value) {
    }
}
