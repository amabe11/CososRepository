package mvvm.model.datasource;

import java.io.IOException;
import java.util.Optional;

/**
 * Fuente de datos cruda: entrega el JSON de una página de personajes tal cual.
 *
 * <p>No sabe nada de mapeo ni de clases de dominio; eso es trabajo del repositorio.</p>
 */
@FunctionalInterface
public interface CharacterDataSource {

    /**
     * @param page      número de página (empieza en 1)
     * @param nameQuery filtro por nombre; vacío para no filtrar
     * @return el JSON, o vacío si la API responde que no hay resultados
     * @throws IOException si no se pudo obtener la respuesta (sin red, timeout, error HTTP…)
     */
    Optional<String> fetchPage(int page, String nameQuery) throws IOException;
}
