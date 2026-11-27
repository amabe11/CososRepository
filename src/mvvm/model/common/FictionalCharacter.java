package mvvm.model.common;

import java.util.List;
import java.util.Optional;

/**
 * Contrato común a los personajes de ambas APIs.
 *
 * <p>Rick and Morty y Attack on Titan devuelven JSON muy distintos, pero los dos
 * tienen id, nombre, estado, género, imagen y episodios. Gracias a esta interfaz
 * el repositorio y el ViewModel pueden ser genéricos y no saben de qué API vienen
 * los datos.</p>
 */
public interface FictionalCharacter {

    int id();

    String name();

    LifeStatus status();

    Gender gender();

    /** URL de la imagen; vacío si la API no la trae. */
    Optional<String> imageUrl();

    /** Números de episodio en los que aparece (sacados de las URLs de la API). */
    List<Integer> episodeIds();
}
