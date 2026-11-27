package mvvm.model.repository;

/** Error del repositorio con un mensaje listo para mostrar al usuario. */
public class RepositoryException extends Exception {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
