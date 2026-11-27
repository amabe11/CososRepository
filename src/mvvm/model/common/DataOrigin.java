package mvvm.model.common;

/** De dónde salieron los datos de una página. */
public enum DataOrigin {
    /** Respuesta de la API en línea. */
    REMOTE,
    /** Archivo JSON incluido en el proyecto (se usa cuando no hay conexión). */
    LOCAL
}
