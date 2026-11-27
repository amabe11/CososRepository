package mvvm.model.attackontitan;

import mvvm.core.json.JsonObject;
import mvvm.model.attackontitan.AttackOnTitanCharacter.Family;
import mvvm.model.attackontitan.AttackOnTitanCharacter.Group;
import mvvm.model.attackontitan.AttackOnTitanDto.CharacterDto;
import mvvm.model.attackontitan.AttackOnTitanDto.GroupDto;
import mvvm.model.attackontitan.AttackOnTitanDto.InfoDto;
import mvvm.model.attackontitan.AttackOnTitanDto.RelativeDto;
import mvvm.model.attackontitan.AttackOnTitanDto.ResponseDto;
import mvvm.model.common.ApiText;
import mvvm.model.common.DataOrigin;
import mvvm.model.common.Gender;
import mvvm.model.common.LifeStatus;
import mvvm.model.common.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Mapea la Attack on Titan API en dos pasos:
 * <ol>
 *     <li><b>JSON → DTO</b>: copia los campos; tolera {@code img} ausente y {@code age} de tipo variable.</li>
 *     <li><b>DTO → dominio</b>: "unknown" → vacío, edad → número, quita notas "[n]" de la wiki,
 *         "Deceased" → {@link LifeStatus#DEAD}, URLs de episodios → números y familiares
 *         que vienen como URL → ids de personaje.</li>
 * </ol>
 */
public final class AttackOnTitanMapper {

    private AttackOnTitanMapper() {
    }

    // ---------------------------------------------------------------- JSON → DTO

    public static ResponseDto parseResponse(String json) {
        JsonObject root = JsonObject.parse(json);
        InfoDto info = root.optObject("info")
                .map(AttackOnTitanMapper::toInfoDto)
                .orElse(new InfoDto(0, 0, null, null));
        // Sin resultados la API omite "results": objects() devuelve lista vacía.
        List<CharacterDto> results = root.objects("results").stream()
                .map(AttackOnTitanMapper::toCharacterDto)
                .toList();
        return new ResponseDto(info, results);
    }

    static InfoDto toInfoDto(JsonObject json) {
        return new InfoDto(
                json.integer("count"),
                json.integer("pages"),
                json.optString("next_page").orElse(null),
                json.optString("prev_page").orElse(null));
    }

    static CharacterDto toCharacterDto(JsonObject json) {
        return new CharacterDto(
                json.integer("id"),
                json.string("name"),
                json.optString("img").orElse(null),
                json.strings("alias"),
                json.strings("species"),
                json.optString("gender").orElse(null),
                json.optScalarAsText("age").orElse(null),
                json.optString("height").orElse(null),
                json.objects("relatives").stream().map(AttackOnTitanMapper::toRelativeDto).toList(),
                json.optString("birthplace").orElse(null),
                json.optString("residence").orElse(null),
                json.optString("status").orElse(null),
                json.optString("occupation").orElse(null),
                json.objects("groups").stream().map(AttackOnTitanMapper::toGroupDto).toList(),
                json.strings("roles"),
                json.strings("episodes"));
    }

    static RelativeDto toRelativeDto(JsonObject json) {
        return new RelativeDto(json.optString("family").orElse(""), json.strings("members"));
    }

    static GroupDto toGroupDto(JsonObject json) {
        return new GroupDto(json.optString("name").orElse(""), json.strings("sub_groups"));
    }

    // ---------------------------------------------------------------- DTO → dominio

    public static AttackOnTitanCharacter toDomain(CharacterDto dto) {
        return new AttackOnTitanCharacter(
                dto.id(),
                ApiText.clean(dto.name()),
                ApiText.url(dto.img()),
                ApiText.knownAll(dto.alias()),
                ApiText.knownAll(dto.species()),
                Gender.fromApi(dto.gender()),
                parseAge(dto.age()),
                ApiText.known(dto.height()),
                dto.relatives().stream().map(AttackOnTitanMapper::toFamily).toList(),
                ApiText.known(dto.birthplace()),
                ApiText.known(dto.residence()),
                LifeStatus.fromApi(dto.status()),
                ApiText.known(dto.occupation()),
                dto.groups().stream().map(AttackOnTitanMapper::toGroup).toList(),
                ApiText.knownAll(dto.roles()),
                ApiText.trailingIds(dto.episodes()));
    }

    /** Separa los miembros que son URL de otro personaje de los que son un nombre. */
    static Family toFamily(RelativeDto dto) {
        List<String> names = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (String member : dto.members()) {
            OptionalInt id = member.startsWith("http") ? ApiText.trailingId(member) : OptionalInt.empty();
            if (id.isPresent()) {
                ids.add(id.getAsInt());
            } else {
                ApiText.known(member).ifPresent(names::add);
            }
        }
        return new Family(ApiText.clean(dto.family()), names, ids);
    }

    static Group toGroup(GroupDto dto) {
        return new Group(ApiText.clean(dto.name()), ApiText.knownAll(dto.subGroups()));
    }

    /** {@code "19"} → 19; {@code "unknown"} o cualquier texto no numérico → vacío. */
    static OptionalInt parseAge(String raw) {
        Optional<String> text = ApiText.known(raw);
        if (text.isEmpty()) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(text.get()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static Page<AttackOnTitanCharacter> toPage(ResponseDto dto, int pageNumber, DataOrigin origin) {
        List<AttackOnTitanCharacter> characters = dto.results().stream()
                .map(AttackOnTitanMapper::toDomain)
                .toList();
        return new Page<>(characters, pageNumber, dto.info().pages(), dto.info().count(), origin);
    }
}
