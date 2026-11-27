package mvvm.model.rickandmorty;

import mvvm.core.json.JsonException;
import mvvm.core.json.JsonObject;
import mvvm.model.common.ApiText;
import mvvm.model.common.DataOrigin;
import mvvm.model.common.Gender;
import mvvm.model.common.LifeStatus;
import mvvm.model.common.Page;
import mvvm.model.rickandmorty.RickAndMortyCharacter.Place;
import mvvm.model.rickandmorty.RickAndMortyDto.CharacterDto;
import mvvm.model.rickandmorty.RickAndMortyDto.InfoDto;
import mvvm.model.rickandmorty.RickAndMortyDto.LocationRefDto;
import mvvm.model.rickandmorty.RickAndMortyDto.ResponseDto;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Mapea la Rick and Morty API en dos pasos:
 * <ol>
 *     <li><b>JSON → DTO</b>: copia los campos tal cual vienen.</li>
 *     <li><b>DTO → dominio</b>: normaliza estado y género, convierte "unknown"/"" en vacío,
 *         saca ids de las URLs y convierte la fecha a {@link Instant}.</li>
 * </ol>
 */
public final class RickAndMortyMapper {

    private RickAndMortyMapper() {
    }

    // ---------------------------------------------------------------- JSON → DTO

    public static ResponseDto parseResponse(String json) {
        JsonObject root = JsonObject.parse(json);
        InfoDto info = root.optObject("info")
                .map(RickAndMortyMapper::toInfoDto)
                .orElse(new InfoDto(0, 0, null, null));
        List<CharacterDto> results = root.objects("results").stream()
                .map(RickAndMortyMapper::toCharacterDto)
                .toList();
        return new ResponseDto(info, results);
    }

    static InfoDto toInfoDto(JsonObject json) {
        return new InfoDto(
                json.integer("count"),
                json.integer("pages"),
                json.optString("next").orElse(null),
                json.optString("prev").orElse(null));
    }

    static CharacterDto toCharacterDto(JsonObject json) {
        return new CharacterDto(
                json.integer("id"),
                json.string("name"),
                json.string("status"),
                json.string("species"),
                json.optString("type").orElse(""),
                json.string("gender"),
                toLocationRefDto(json.object("origin")),
                toLocationRefDto(json.object("location")),
                json.string("image"),
                json.strings("episode"),
                json.string("url"),
                json.string("created"));
    }

    static LocationRefDto toLocationRefDto(JsonObject json) {
        return new LocationRefDto(json.string("name"), json.optString("url").orElse(""));
    }

    // ---------------------------------------------------------------- DTO → dominio

    public static RickAndMortyCharacter toDomain(CharacterDto dto) {
        return new RickAndMortyCharacter(
                dto.id(),
                ApiText.clean(dto.name()),
                LifeStatus.fromApi(dto.status()),
                ApiText.known(dto.species()),
                ApiText.known(dto.type()),
                Gender.fromApi(dto.gender()),
                toPlace(dto.origin()),
                toPlace(dto.location()),
                dto.image(),
                ApiText.trailingIds(dto.episode()),
                dto.url(),
                parseDate(dto.created()));
    }

    static Place toPlace(LocationRefDto dto) {
        return new Place(ApiText.known(dto.name()), ApiText.trailingId(dto.url()));
    }

    public static Page<RickAndMortyCharacter> toPage(ResponseDto dto, int pageNumber, DataOrigin origin) {
        List<RickAndMortyCharacter> characters = dto.results().stream()
                .map(RickAndMortyMapper::toDomain)
                .toList();
        return new Page<>(characters, pageNumber, dto.info().pages(), dto.info().count(), origin);
    }

    private static Instant parseDate(String text) {
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            throw new JsonException("La fecha 'created' no es ISO-8601: " + text);
        }
    }
}
