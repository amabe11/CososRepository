package mvvm.model.rickandmorty;

import mvvm.model.common.FictionalCharacter;
import mvvm.model.common.Gender;
import mvvm.model.common.LifeStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Personaje de Rick and Morty ya limpio (modelo de dominio).
 *
 * @param species    especie; vacío si es "unknown"
 * @param subtype    campo {@code type} del JSON; vacío si venía {@code ""}
 * @param origin     lugar de origen
 * @param location   última ubicación conocida
 * @param image      URL del avatar (300×300 JPEG)
 * @param episodeIds números de episodio sacados de las URLs
 * @param url        URL del personaje en la API
 * @param created    fecha en que se creó el registro en la API
 */
public record RickAndMortyCharacter(
        int id,
        String name,
        LifeStatus status,
        Optional<String> species,
        Optional<String> subtype,
        Gender gender,
        Place origin,
        Place location,
        String image,
        List<Integer> episodeIds,
        String url,
        Instant created) implements FictionalCharacter {

    public RickAndMortyCharacter {
        episodeIds = List.copyOf(episodeIds);
    }

    /**
     * Lugar referenciado por un personaje.
     *
     * @param name nombre; vacío si es "unknown"
     * @param id   id del lugar sacado de {@code ".../location/3"}; vacío si la URL venía en blanco
     */
    public record Place(Optional<String> name, OptionalInt id) {
    }

    @Override
    public Optional<String> imageUrl() {
        return Optional.ofNullable(image).filter(url -> !url.isBlank());
    }
}
