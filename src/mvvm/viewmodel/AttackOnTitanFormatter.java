package mvvm.viewmodel;

import mvvm.model.attackontitan.AttackOnTitanCharacter;
import mvvm.model.attackontitan.AttackOnTitanCharacter.Family;
import mvvm.model.attackontitan.AttackOnTitanCharacter.Group;
import mvvm.viewmodel.CharacterDetail.DetailRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Presenta un {@link AttackOnTitanCharacter} en la interfaz. */
public final class AttackOnTitanFormatter implements CharacterFormatter<AttackOnTitanCharacter> {

    @Override
    public CharacterItem toItem(AttackOnTitanCharacter c) {
        return new CharacterItem(c.name(), subtitle(c), Labels.status(c.status()), Labels.tone(c.status()), c.imageUrl());
    }

    @Override
    public CharacterDetail toDetail(AttackOnTitanCharacter c) {
        List<DetailRow> rows = List.of(
                new DetailRow("ID", "#" + c.id()),
                new DetailRow("Alias", Labels.join(c.aliases())),
                new DetailRow("Especie", c.species().isEmpty() ? Labels.NO_DATA : String.join(", ", c.species())),
                new DetailRow("Género", Labels.gender(c.gender())),
                new DetailRow("Edad", c.age().isPresent() ? c.age().getAsInt() + " años" : Labels.NO_DATA),
                new DetailRow("Altura", Labels.orNoData(c.height())),
                new DetailRow("Lugar de nacimiento", Labels.orNoData(c.birthplace())),
                new DetailRow("Residencia", Labels.orNoData(c.residence())),
                new DetailRow("Ocupación", Labels.orNoData(c.occupation())),
                new DetailRow("Grupos", groups(c.groups())),
                new DetailRow("Roles", Labels.join(c.roles())),
                new DetailRow("Familia", families(c.relatives())),
                new DetailRow("Episodios", Labels.episodes(c.episodeIds())));
        return new CharacterDetail(c.name(), subtitle(c), Labels.status(c.status()), Labels.tone(c.status()),
                c.imageUrl(), rows);
    }

    private static String subtitle(AttackOnTitanCharacter c) {
        String species = c.species().isEmpty() ? "Especie desconocida" : String.join(", ", c.species());
        return species + " · " + Labels.gender(c.gender());
    }

    /** "Scout Regiment (Special Operations Squad); Military Police" */
    private static String groups(List<Group> groups) {
        if (groups.isEmpty()) {
            return Labels.NONE;
        }
        return groups.stream()
                .map(g -> g.subGroups().isEmpty() ? g.name() : g.name() + " (" + String.join(", ", g.subGroups()) + ")")
                .collect(Collectors.joining("; "));
    }

    /** Una familia por línea: "Ackermann family: Unnamed relative · personajes #165, #57". */
    private static String families(List<Family> families) {
        if (families.isEmpty()) {
            return Labels.NONE;
        }
        return families.stream()
                .map(AttackOnTitanFormatter::family)
                .collect(Collectors.joining("\n"));
    }

    private static String family(Family f) {
        List<String> parts = new ArrayList<>();
        if (!f.members().isEmpty()) {
            parts.add(String.join(", ", f.members()));
        }
        if (!f.memberIds().isEmpty()) {
            String ids = f.memberIds().stream().map(id -> "#" + id).collect(Collectors.joining(", "));
            parts.add((f.memberIds().size() == 1 ? "personaje " : "personajes ") + ids);
        }
        return parts.isEmpty() ? f.name() : f.name() + ": " + String.join(" · ", parts);
    }
}
