package mvvm.core.json;

/** Error al leer un documento JSON o al mapearlo a clases Java. */
public class JsonException extends RuntimeException {

    public JsonException(String message) {
        super(message);
    }
}
