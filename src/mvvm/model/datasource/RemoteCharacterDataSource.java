package mvvm.model.datasource;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Lee personajes de una API REST paginada con {@code ?page=N&name=texto}.
 *
 * <p>Ambas APIs aceptan esos parámetros, pero responden distinto cuando no hay resultados:</p>
 * <ul>
 *     <li>Rick and Morty: HTTP 404 con {@code {"error":"There is nothing here"}}</li>
 *     <li>Attack on Titan: HTTP 200 con {@code info.count = 0} y sin la clave {@code results}</li>
 * </ul>
 * Por eso un 404 se trata como "sin resultados" y no como error.
 */
public final class RemoteCharacterDataSource implements CharacterDataSource {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient http;
    private final String endpoint;

    /**
     * @param http     cliente HTTP compartido
     * @param endpoint URL del recurso de personajes, por ejemplo {@code https://rickandmortyapi.com/api/character}
     */
    public RemoteCharacterDataSource(HttpClient http, String endpoint) {
        this.http = Objects.requireNonNull(http, "http");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
    }

    @Override
    public Optional<String> fetchPage(int page, String nameQuery) throws IOException {
        URI uri = buildUri(page, nameQuery);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status == 404) {
                return Optional.empty();
            }
            if (status == 429) {
                // Rick and Morty limita la cantidad de peticiones por minuto.
                throw new IOException("la API está limitando las peticiones (HTTP 429); espera unos segundos");
            }
            if (status < 200 || status >= 300) {
                throw new IOException("la API respondió HTTP " + status);
            }
            return Optional.of(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("la petición fue interrumpida", e);
        }
    }

    URI buildUri(int page, String nameQuery) {
        StringBuilder url = new StringBuilder(endpoint).append("?page=").append(page);
        if (nameQuery != null && !nameQuery.isBlank()) {
            String encoded = URLEncoder.encode(nameQuery.strip(), StandardCharsets.UTF_8).replace("+", "%20");
            url.append("&name=").append(encoded);
        }
        return URI.create(url.toString());
    }
}
