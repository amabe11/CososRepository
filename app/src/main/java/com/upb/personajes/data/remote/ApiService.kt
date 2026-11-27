package com.upb.personajes.data.remote

import com.google.gson.JsonElement
import com.upb.personajes.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*
 * ApiService: las dos APIs del proyecto en un solo lugar.
 *
 *   ApiService
 *   ├── global ............ ConfiguracionGlobal  → se aplica a TODAS las APIs
 *   ├── rickAndMorty ...... ApiRemota<RickAndMortyApi>   ┐ cada una con su baseUrl, su propia
 *   └── attackOnTitan ..... ApiRemota<AttackOnTitanApi>  ┘ instancia de Retrofit y sus propios valores
 *
 * Cómo se combinan los valores: cada API usa su valor propio si lo tiene; si lo deja en null,
 * usa el global. Las cabeceras se suman (si se repite una, gana la de la API).
 *
 * Ejemplos:
 *   // Global: las dos APIs pasan a esperar 30 s
 *   ApiService.configurarGlobal { copy(tiempoEsperaSegundos = 30) }
 *
 *   // Solo Attack on Titan: 60 s. Rick and Morty sigue con el valor global
 *   ApiService.attackOnTitan.configurar { copy(tiempoEsperaSegundos = 60) }
 *
 *   // Solo Rick and Morty: otra baseUrl (p. ej. un servidor de pruebas)
 *   ApiService.rickAndMorty.configurar { copy(baseUrl = "http://10.0.2.2:8080/api/") }
 *
 *   // Usar una API desde un repositorio (regla de oro: solo el repositorio llama a Retrofit)
 *   ApiService.rickAndMorty.servicio.obtenerPersonajes(pagina = 1)
 *
 * Cambiar una configuración no rompe lo que ya está en curso: la próxima vez que se pida
 * `servicio` se crea una instancia nueva de Retrofit con los valores nuevos.
 */

// ─────────────────────────────── Endpoints (MANUAL-RETROFIT, paso 6)

/** Endpoints de https://rickandmortyapi.com/api/ */
interface RickAndMortyApi {

    /** Lista paginada: character?page=1&name=rick. Si `nombre` es null, Retrofit no agrega `name`. */
    @GET("character")
    suspend fun obtenerPersonajes(
        @Query("page") pagina: Int,
        @Query("name") nombre: String? = null,
    ): RickAndMortyRespuestaDto

    /** Detalle: character/2. Si no existe responde 404. */
    @GET("character/{id}")
    suspend fun obtenerPersonaje(@Path("id") id: Int): RickAndMortyPersonajeDto
}

/** Endpoints de https://api.attackontitanapi.com/ */
interface AttackOnTitanApi {

    /** Lista paginada: characters?page=1&name=eren. Sin resultados responde 200 sin "results". */
    @GET("characters")
    suspend fun obtenerPersonajes(
        @Query("page") pagina: Int,
        @Query("name") nombre: String? = null,
    ): AttackOnTitanRespuestaDto

    /**
     * Detalle: characters/5.
     * OJO: si el id no existe responde 200 con `[]` (un arreglo, no un objeto). Con un DTO como
     * tipo de retorno Gson fallaría ("Expected BEGIN_OBJECT but was BEGIN_ARRAY"), por eso se
     * recibe un JsonElement y el repositorio revisa qué llegó.
     */
    @GET("characters/{id}")
    suspend fun obtenerPersonaje(@Path("id") id: Int): JsonElement
}

// ─────────────────────────────── Configuración

/** Valores que comparten todas las APIs. */
data class ConfiguracionGlobal(
    /** Tiempo máximo para conectar, enviar y recibir (manual, paso 7: sin esto la app puede cargar sin fin). */
    val tiempoEsperaSegundos: Long = 15,
    /** Registro en Logcat. Solo en debug: la app publicada no debe registrar las peticiones. */
    val nivelRegistro: HttpLoggingInterceptor.Level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE,
    /** Cómo se identifica la app ante los servidores. */
    val agenteDeUsuario: String = "PersonajesMVVM/1.0 (Android)",
    /** Cabeceras que viajan en todas las peticiones. */
    val cabeceras: Map<String, String> = emptyMap(),
)

/** Valores propios de una API. Lo que quede en null se toma de [ConfiguracionGlobal]. */
data class ConfiguracionApi(
    /** Debe terminar en "/" (manual, paso 7: si no, Retrofit lanza IllegalArgumentException). */
    val baseUrl: String,
    val tiempoEsperaSegundos: Long? = null,
    val nivelRegistro: HttpLoggingInterceptor.Level? = null,
    val agenteDeUsuario: String? = null,
    /** Se suman a las cabeceras globales; si se repite una, gana esta. */
    val cabeceras: Map<String, String> = emptyMap(),
) {
    init {
        require(baseUrl.endsWith("/")) { "La baseUrl debe terminar en \"/\": $baseUrl" }
        require(tiempoEsperaSegundos == null || tiempoEsperaSegundos > 0) { "El tiempo de espera debe ser positivo" }
    }
}

/** Lo que finalmente usa una API: sus valores propios completados con los globales. */
data class ConfiguracionEfectiva(
    val baseUrl: String,
    val tiempoEsperaSegundos: Long,
    val nivelRegistro: HttpLoggingInterceptor.Level,
    val agenteDeUsuario: String,
    val cabeceras: Map<String, String>,
)

fun ConfiguracionApi.combinarCon(global: ConfiguracionGlobal) = ConfiguracionEfectiva(
    baseUrl = baseUrl,
    tiempoEsperaSegundos = tiempoEsperaSegundos ?: global.tiempoEsperaSegundos,
    nivelRegistro = nivelRegistro ?: global.nivelRegistro,
    agenteDeUsuario = agenteDeUsuario ?: global.agenteDeUsuario,
    cabeceras = global.cabeceras + cabeceras,
)

// ─────────────────────────────── Una API (independiente de las demás)

/**
 * Una API con su propia configuración y su propia instancia de Retrofit
 * (manual, buenas prácticas: "una sola instancia de Retrofit por baseUrl").
 */
class ApiRemota<S : Any> internal constructor(
    val nombre: String,
    private val configuracionDeFabrica: ConfiguracionApi,
    private val tipoServicio: Class<S>,
    private val global: () -> ConfiguracionGlobal,
) {
    @Volatile
    var configuracion: ConfiguracionApi = configuracionDeFabrica
        private set

    @Volatile
    private var servicioActual: S? = null

    /** Valores que usa esta API ahora mismo (propios + globales). */
    val configuracionEfectiva: ConfiguracionEfectiva
        get() = configuracion.combinarCon(global())

    /** El servicio de Retrofit. Se crea la primera vez que se pide y se reutiliza. */
    val servicio: S
        get() = servicioActual ?: synchronized(this) {
            servicioActual ?: RetrofitInstance.crear(configuracionEfectiva).create(tipoServicio)
                .also { servicioActual = it }
        }

    /** Cambia solo esta API. Ej.: `configurar { copy(tiempoEsperaSegundos = 60) }` */
    fun configurar(cambio: ConfiguracionApi.() -> ConfiguracionApi) {
        synchronized(this) {
            configuracion = configuracion.cambio()
            servicioActual = null
        }
    }

    /** Vuelve a los valores con los que se declaró en [ApiService]. */
    fun restablecer() = configurar { configuracionDeFabrica }

    /** La configuración global cambió: la próxima llamada crea Retrofit de nuevo. */
    internal fun invalidar() {
        synchronized(this) { servicioActual = null }
    }
}

// ─────────────────────────────── Las dos APIs juntas

object ApiService {

    @Volatile
    var global: ConfiguracionGlobal = ConfiguracionGlobal()
        private set

    val rickAndMorty: ApiRemota<RickAndMortyApi> = ApiRemota(
        nombre = "Rick and Morty",
        configuracionDeFabrica = ConfiguracionApi(baseUrl = "https://rickandmortyapi.com/api/"),
        tipoServicio = RickAndMortyApi::class.java,
        global = { global },
    )

    val attackOnTitan: ApiRemota<AttackOnTitanApi> = ApiRemota(
        nombre = "Attack on Titan",
        configuracionDeFabrica = ConfiguracionApi(baseUrl = "https://api.attackontitanapi.com/"),
        tipoServicio = AttackOnTitanApi::class.java,
        global = { global },
    )

    val todas: List<ApiRemota<*>> get() = listOf(rickAndMorty, attackOnTitan)

    /** Cambia un valor para TODAS las APIs. Ej.: `configurarGlobal { copy(tiempoEsperaSegundos = 30) }` */
    fun configurarGlobal(cambio: ConfiguracionGlobal.() -> ConfiguracionGlobal) {
        synchronized(this) { global = global.cambio() }
        todas.forEach { it.invalidar() }
    }

    /** Todo a los valores de fábrica: global y cada API. */
    fun restablecer() {
        configurarGlobal { ConfiguracionGlobal() }
        todas.forEach { it.restablecer() }
    }
}
