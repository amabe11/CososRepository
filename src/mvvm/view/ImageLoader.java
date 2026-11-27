package mvvm.view;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Descarga y escala imágenes en segundo plano, con caché en memoria.
 *
 * <p>Es un detalle de la View (píxeles e íconos), por eso no está en el ViewModel:
 * el ViewModel solo expone la URL.</p>
 */
public final class ImageLoader {

    /** Identifica a la aplicación ante los servidores de imágenes. */
    private static final String USER_AGENT = "MVVM-Character-Explorer/1.0";

    private final HttpClient http;
    private final Executor executor;
    private final Map<String, CompletableFuture<BufferedImage>> downloads = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Icon>> icons = new ConcurrentHashMap<>();

    public ImageLoader(HttpClient http, Executor executor) {
        this.http = http;
        this.executor = executor;
    }

    /** Ícono ya listo, o {@code null} si todavía no se cargó (o falló). */
    Icon cachedIcon(String url, int width, int height, boolean cropSquare) {
        CompletableFuture<Icon> icon = icons.get(key(url, width, height, cropSquare));
        return icon != null && icon.isDone() && !icon.isCompletedExceptionally() ? icon.join() : null;
    }

    /**
     * Carga la imagen (reutilizando descargas anteriores) y avisa en el hilo de Swing:
     * {@code onReady} con el ícono, o {@code onFailed} si no se pudo descargar o leer.
     */
    void loadIcon(String url, int width, int height, boolean cropSquare, Consumer<Icon> onReady, Runnable onFailed) {
        icons.computeIfAbsent(key(url, width, height, cropSquare),
                        k -> download(url).thenApplyAsync(image -> scale(image, width, height, cropSquare), executor))
                .whenComplete((icon, error) -> SwingUtilities.invokeLater(() -> {
                    if (error == null) {
                        onReady.accept(icon);
                    } else {
                        onFailed.run();
                    }
                }));
    }

    /** {@code true} si esa imagen ya se intentó descargar y falló. */
    boolean hasFailed(String url, int width, int height, boolean cropSquare) {
        CompletableFuture<Icon> icon = icons.get(key(url, width, height, cropSquare));
        return icon != null && icon.isCompletedExceptionally();
    }

    private CompletableFuture<BufferedImage> download(String url) {
        return downloads.computeIfAbsent(url, u -> CompletableFuture.supplyAsync(() -> fetch(u), executor));
    }

    private BufferedImage fetch(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(downloadableUrl(url)))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "image/png,image/jpeg,image/gif;q=0.9,*/*;q=0.1")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() / 100 != 2) {
                throw new IOException("HTTP " + response.statusCode() + " al descargar " + url);
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.body()));
            if (image == null) {
                throw new IOException("formato de imagen no soportado: " + url);
            }
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        }
    }

    /**
     * Las imágenes de Attack on Titan vienen de Fandom ({@code static.wikia.nocookie.net}), que las
     * convierte a WebP aunque se pida PNG. ImageIO no lee WebP, así que se pide el formato original.
     *
     * <p>Ojo: ese CDN está detrás de la protección anti-bots de Cloudflare y puede responder 403
     * ("Just a moment...") al cliente HTTP de Java. En ese caso la View muestra las iniciales.</p>
     */
    static String downloadableUrl(String url) {
        if (url.contains("static.wikia.nocookie.net") && !url.contains("format=")) {
            return url + (url.contains("?") ? "&" : "?") + "format=original";
        }
        return url;
    }

    /**
     * Escala manteniendo la proporción. Con {@code cropSquare} recorta un cuadrado
     * desde arriba (donde suelen estar las caras) para las miniaturas.
     */
    private static Icon scale(BufferedImage source, int width, int height, boolean cropSquare) {
        BufferedImage image = source;
        if (cropSquare) {
            int side = Math.min(source.getWidth(), source.getHeight());
            image = source.getSubimage((source.getWidth() - side) / 2, 0, side, side);
        }
        double ratio = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
        int w = Math.max(1, (int) Math.round(image.getWidth() * ratio));
        int h = Math.max(1, (int) Math.round(image.getHeight() * ratio));
        // ImageIcon espera a que la imagen escalada esté completa.
        return new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private static String key(String url, int width, int height, boolean cropSquare) {
        return url + '@' + width + 'x' + height + (cropSquare ? "c" : "");
    }
}
