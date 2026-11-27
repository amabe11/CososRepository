package mvvm.core.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Objeto JSON de solo lectura con getters tipados.
 *
 * <p>Es la herramienta que usan los mappers para pasar del JSON a los DTO:
 * los métodos "obligatorios" ({@link #string}, {@link #integer}, {@link #object})
 * fallan con un mensaje claro si la API cambia de formato, y los métodos
 * {@code opt*} devuelven vacío cuando la clave no existe o vale {@code null}.</p>
 */
public final class JsonObject {

    private final Map<String, Object> values;

    private JsonObject(Map<String, Object> values) {
        this.values = values;
    }

    /** Lee un documento cuya raíz debe ser un objeto JSON. */
    public static JsonObject parse(String json) {
        return from(JsonParser.parse(json), "(raíz)");
    }

    @SuppressWarnings("unchecked")
    private static JsonObject from(Object value, String path) {
        if (value instanceof Map<?, ?> map) {
            return new JsonObject((Map<String, Object>) map);
        }
        throw new JsonException("Se esperaba un objeto en '" + path + "' pero es " + describe(value));
    }

    /** {@code true} si la clave existe y no vale {@code null}. */
    public boolean has(String key) {
        return values.get(key) != null;
    }

    /** Texto obligatorio. */
    public String string(String key) {
        if (values.get(key) instanceof String s) {
            return s;
        }
        throw typeError(key, "texto");
    }

    /** Texto opcional. */
    public Optional<String> optString(String key) {
        Object value = values.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof String s) {
            return Optional.of(s);
        }
        throw typeError(key, "texto");
    }

    /**
     * Valor escalar (texto, número o booleano) convertido a texto.
     * Sirve para campos cuyo tipo cambia según el registro, como {@code age}
     * en Attack on Titan, que puede ser {@code 19} o {@code "unknown"}.
     */
    public Optional<String> optScalarAsText(String key) {
        Object value = values.get(key);
        return switch (value) {
            case null -> Optional.empty();
            case String s -> Optional.of(s);
            case Number n -> Optional.of(n.toString());
            case Boolean b -> Optional.of(b.toString());
            default -> throw typeError(key, "texto o número");
        };
    }

    /** Número entero obligatorio. */
    public int integer(String key) {
        if (values.get(key) instanceof Number n) {
            return n.intValue();
        }
        throw typeError(key, "número");
    }

    /** Objeto anidado obligatorio. */
    public JsonObject object(String key) {
        if (!has(key)) {
            throw typeError(key, "objeto");
        }
        return from(values.get(key), key);
    }

    /** Objeto anidado opcional. */
    public Optional<JsonObject> optObject(String key) {
        return has(key) ? Optional.of(from(values.get(key), key)) : Optional.empty();
    }

    /** Arreglo de objetos. Si la clave no existe devuelve una lista vacía. */
    public List<JsonObject> objects(String key) {
        List<JsonObject> result = new ArrayList<>();
        for (Object item : list(key)) {
            result.add(from(item, key + "[]"));
        }
        return List.copyOf(result);
    }

    /** Arreglo de textos. Si la clave no existe devuelve una lista vacía. */
    public List<String> strings(String key) {
        List<String> result = new ArrayList<>();
        for (Object item : list(key)) {
            if (!(item instanceof String s)) {
                throw new JsonException("'" + key + "' debería contener solo textos pero tiene " + describe(item));
            }
            result.add(s);
        }
        return List.copyOf(result);
    }

    private List<?> list(String key) {
        Object value = values.get(key);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }
        throw typeError(key, "arreglo");
    }

    private JsonException typeError(String key, String expected) {
        return new JsonException("La propiedad '" + key + "' debería ser " + expected
                + " pero es " + describe(values.get(key)));
    }

    private static String describe(Object value) {
        return switch (value) {
            case null -> "null o inexistente";
            case String s -> "texto \"" + s + "\"";
            case Number n -> "número " + n;
            case Boolean b -> "booleano " + b;
            case List<?> l -> "arreglo";
            case Map<?, ?> m -> "objeto";
            default -> value.getClass().getSimpleName();
        };
    }
}
