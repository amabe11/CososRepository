package mvvm.model.attackontitan;

import mvvm.model.common.DataOrigin;
import mvvm.model.common.Page;
import mvvm.model.datasource.CharacterDataSource;
import mvvm.model.repository.ApiCharacterRepository;

/** Repositorio de personajes de Attack on Titan. */
public final class AttackOnTitanRepository extends ApiCharacterRepository<AttackOnTitanCharacter> {

    /** Endpoint de personajes. Ficha de la API: https://www.freepublicapis.com/attack-on-titan-api */
    public static final String API_URL = "https://api.attackontitanapi.com/characters";

    /** Copia local de la página 1 (carpeta {@code resources/}). */
    public static final String LOCAL_FILE = "/characters_aot.json";

    public AttackOnTitanRepository(CharacterDataSource remote, CharacterDataSource localFallback) {
        super(remote, localFallback);
    }

    @Override
    protected Page<AttackOnTitanCharacter> parsePage(String json, int pageNumber, DataOrigin origin) {
        return AttackOnTitanMapper.toPage(AttackOnTitanMapper.parseResponse(json), pageNumber, origin);
    }
}
