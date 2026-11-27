package com.upb.personajes

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.upb.personajes.data.local.PersonajesLocalRepository
import com.upb.personajes.data.remote.RetrofitInstance
import com.upb.personajes.model.GestorDePersonajes

/**
 * Se crea antes que cualquier pantalla. Aquí vive la única instancia del Modelo,
 * que los ViewModels reciben por su fábrica.
 */
class PersonajesApp : Application(), ImageLoaderFactory {

    val gestor: GestorDePersonajes by lazy {
        GestorDePersonajes(local = PersonajesLocalRepository(this))
    }

    /** Las imágenes se descargan con los valores globales de ApiService (agente de usuario, tiempos). */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient { RetrofitInstance.clienteImagenes() }
            .crossfade(true)
            .build()
}
