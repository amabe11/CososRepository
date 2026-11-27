package mvvm.model.rickandmorty;

import mvvm.model.common.DataOrigin;
import mvvm.model.common.Page;
import mvvm.model.datasource.CharacterDataSource;
import mvvm.model.repository.ApiCharacterRepository;

/** Repositorio de personajes de Rick and Morty. */
public final class RickAndMortyRepository extends ApiCharacterRepository<RickAndMortyCharacter> {

    /** Endpoint de personajes. Ficha de la API: https://www.freepublicapis.com/rick-and-morty-api */
    public static final String API_URL = "https://rickandmortyapi.com/api/character";

    /** Copia local de la página 1 (carpeta {@code resources/}). */
    public static final String LOCAL_FILE = "/character_rick&morty.json";

    public RickAndMortyRepository(CharacterDataSource remote, CharacterDataSource localFallback) {
        super(remote, localFallback);
    }

    @Override
    protected Page<RickAndMortyCharacter> parsePage(String json, int pageNumber, DataOrigin origin) {
        return RickAndMortyMapper.toPage(RickAndMortyMapper.parseResponse(json), pageNumber, origin);
    }
}
