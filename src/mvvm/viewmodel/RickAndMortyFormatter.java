package mvvm.viewmodel;

import mvvm.model.rickandmorty.RickAndMortyCharacter;
import mvvm.viewmodel.CharacterDetail.DetailRow;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Presenta un {@link RickAndMortyCharacter} en la interfaz. */
public final class RickAndMortyFormatter implements CharacterFormatter<RickAndMortyCharacter> {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneOffset.UTC);

    @Override
    public CharacterItem toItem(RickAndMortyCharacter c) {
        return new CharacterItem(c.name(), subtitle(c), Labels.status(c.status()), Labels.tone(c.status()), c.imageUrl());
    }

    @Override
    public CharacterDetail toDetail(RickAndMortyCharacter c) {
        List<DetailRow> rows = List.of(
                new DetailRow("ID", "#" + c.id()),
                new DetailRow("Especie", Labels.orNoData(c.species())),
                new DetailRow("Tipo", c.subtype().orElse(Labels.NONE)),
                new DetailRow("Género", Labels.gender(c.gender())),
                new DetailRow("Origen", Labels.orNoData(c.origin().name())),
                new DetailRow("Última ubicación", Labels.orNoData(c.location().name())),
                new DetailRow("Episodios", Labels.episodes(c.episodeIds())),
                new DetailRow("Alta en la API", DATE.format(c.created())));
        return new CharacterDetail(c.name(), subtitle(c), Labels.status(c.status()), Labels.tone(c.status()),
                c.imageUrl(), rows);
    }

    private static String subtitle(RickAndMortyCharacter c) {
        return c.species().orElse("Especie desconocida") + " · " + Labels.gender(c.gender());
    }
}
