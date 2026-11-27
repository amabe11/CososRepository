package mvvm.core.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser JSON mínimo (RFC 8259) sin dependencias externas.
 *
 * <p>Convierte el texto en estructuras estándar de Java:</p>
 * <ul>
 *     <li>objeto → {@code Map<String, Object>} (conserva el orden de las claves)</li>
 *     <li>arreglo → {@code List<Object>}</li>
 *     <li>texto → {@code String}</li>
 *     <li>número → {@code Long} (enteros) o {@code Double} (decimales)</li>
 *     <li>booleano → {@code Boolean}, y {@code null} → {@code null}</li>
 * </ul>
 *
 * <p>Normalmente no se usa directamente: {@link JsonObject} lo envuelve con getters tipados.</p>
 */
public final class JsonParser {

    private final String text;
    private int pos;

    private JsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String json) {
        if (json == null) {
            throw new JsonException("El JSON es null");
        }
        JsonParser parser = new JsonParser(json);
        if (!json.isEmpty() && json.charAt(0) == '﻿') {
            parser.pos = 1; // BOM de UTF-8
        }
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos != parser.text.length()) {
            throw parser.error("Hay contenido inesperado después del JSON");
        }
        return value;
    }

    private Object readValue() {
        if (pos >= text.length()) {
            throw error("El JSON terminó antes de tiempo");
        }
        char c = text.charAt(pos);
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield readNumber();
                }
                throw error("Carácter inesperado '" + c + "'");
            }
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (tryConsume('}')) {
            return object;
        }
        do {
            skipWhitespace();
            if (pos >= text.length() || text.charAt(pos) != '"') {
                throw error("Se esperaba el nombre de una propiedad");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            object.put(key, readValue());
            skipWhitespace();
        } while (tryConsume(','));
        expect('}');
        return object;
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> array = new ArrayList<>();
        skipWhitespace();
        if (tryConsume(']')) {
            return array;
        }
        do {
            skipWhitespace();
            array.add(readValue());
            skipWhitespace();
        } while (tryConsume(','));
        expect(']');
        return array;
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= text.length()) {
                throw error("Texto sin cerrar");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= text.length()) {
                throw error("Secuencia de escape incompleta");
            }
            char escaped = text.charAt(pos++);
            switch (escaped) {
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/' -> sb.append('/');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'u' -> {
                    if (pos + 4 > text.length()) {
                        throw error("Escape unicode incompleto");
                    }
                    try {
                        sb.append((char) Integer.parseInt(text, pos, pos + 4, 16));
                    } catch (NumberFormatException e) {
                        throw error("Escape unicode inválido");
                    }
                    pos += 4;
                }
                default -> throw error("Secuencia de escape desconocida: \\" + escaped);
            }
        }
    }

    private Number readNumber() {
        int start = pos;
        while (pos < text.length() && isNumberChar(text.charAt(pos))) {
            pos++;
        }
        String number = text.substring(start, pos);
        try {
            boolean decimal = number.indexOf('.') >= 0 || number.indexOf('e') >= 0 || number.indexOf('E') >= 0;
            // Ojo: "decimal ? Double : Long" con el operador ternario convertiría todo a double
            // por promoción numérica, y la edad 19 llegaría como "19.0". Por eso if/else.
            if (decimal) {
                return Double.valueOf(number);
            }
            return Long.valueOf(number);
        } catch (NumberFormatException e) {
            throw error("Número inválido '" + number + "'");
        }
    }

    private static boolean isNumberChar(char c) {
        return (c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E';
    }

    private Object readLiteral(String literal, Object value) {
        if (!text.startsWith(literal, pos)) {
            throw error("Se esperaba '" + literal + "'");
        }
        pos += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (pos < text.length()) {
            char c = text.charAt(pos);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                return;
            }
            pos++;
        }
    }

    private boolean tryConsume(char expected) {
        if (pos < text.length() && text.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        if (!tryConsume(expected)) {
            throw error("Se esperaba '" + expected + "'");
        }
    }

    private JsonException error(String message) {
        return new JsonException(message + " (posición " + pos + ")");
    }
}
