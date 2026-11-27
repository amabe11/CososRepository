package mvvm.model.attackontitan;

import mvvm.model.common.FictionalCharacter;
import mvvm.model.common.Gender;
import mvvm.model.common.LifeStatus;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Personaje de Attack on Titan ya limpio (modelo de dominio).
 *
 * <p>Todo lo que en el JSON valía {@code "unknown"} aquí es {@code Optional.empty()},
 * la edad es un número de verdad y los textos no traen notas de la wiki.</p>
 *
 * @param aliases    otros nombres o formas de titán ("Colossal Titan")
 * @param species    puede tener varias: ["Human", "Intelligent Titan"]
 * @param age        edad en años; vacío si es desconocida
 * @param height     altura tal cual la da la API ("60 m (Colossal Titan form)")
 * @param relatives  familias y sus miembros
 * @param groups     grupos (Scout Regiment…) con sus subgrupos
 * @param roles      roles destacados ("Colossal Titans")
 * @param episodeIds números de episodio sacados de las URLs
 */
public record AttackOnTitanCharacter(
        int id,
        String name,
        Optional<String> imageUrl,
        List<String> aliases,
        List<String> species,
        Gender gender,
        OptionalInt age,
        Optional<String> height,
        List<Family> relatives,
        Optional<String> birthplace,
        Optional<String> residence,
        LifeStatus status,
        Optional<String> occupation,
        List<Group> groups,
        List<String> roles,
        List<Integer> episodeIds) implements FictionalCharacter {

    public AttackOnTitanCharacter {
        aliases = List.copyOf(aliases);
        species = List.copyOf(species);
        relatives = List.copyOf(relatives);
        groups = List.copyOf(groups);
        roles = List.copyOf(roles);
        episodeIds = List.copyOf(episodeIds);
    }

    /**
     * Una familia. En el JSON cada miembro puede ser un nombre ("Unnamed father") o la URL
     * de otro personaje (".../characters/165"); aquí se separan en dos listas.
     *
     * @param members   miembros que vienen como nombre
     * @param memberIds ids de miembros que vienen como URL a otro personaje
     */
    public record Family(String name, List<String> members, List<Integer> memberIds) {
        public Family {
            members = List.copyOf(members);
            memberIds = List.copyOf(memberIds);
        }
    }

    /** Un grupo: "Scout Regiment" → ["Special Operations Squad"]. */
    public record Group(String name, List<String> subGroups) {
        public Group {
            subGroups = List.copyOf(subGroups);
        }
    }
}
