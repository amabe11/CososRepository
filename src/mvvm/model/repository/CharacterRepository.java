package mvvm.model.repository;

import mvvm.model.common.FictionalCharacter;
import mvvm.model.common.Page;

/**
 * Punto de entrada del Model para el ViewModel.
 *
 * <p>El ViewModel solo conoce esta interfaz: no sabe si los datos vienen de internet,
 * de un archivo o de un objeto falso en las pruebas.</p>
 *
 * @param <T> tipo de personaje que devuelve
 */
@FunctionalInterface
public interface CharacterRepository<T extends FictionalCharacter> {

    /**
     * @param page      número de página (empieza en 1)
     * @param nameQuery texto a buscar en el nombre; vacío para traer todos
     */
    Page<T> findCharacters(int page, String nameQuery) throws RepositoryException;
}
