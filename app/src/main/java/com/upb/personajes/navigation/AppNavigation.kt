package com.upb.personajes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.ui.screens.InicioScreen
import com.upb.personajes.ui.screens.PersonajeDetailScreen

/** Rutas de la app. */
object Rutas {
    const val INICIO = "inicio"
    const val DETALLE = "detalle/{fuente}/{id}"

    fun detalle(fuente: FuenteApi, id: Int) = "detalle/${fuente.name}/$id"
}

/** Navegación: lista (con una pestaña por API) → detalle del personaje. */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rutas.INICIO) {
        composable(Rutas.INICIO) {
            InicioScreen(onAbrirDetalle = { fuente, id -> navController.navigate(Rutas.detalle(fuente, id)) })
        }
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(
                navArgument("fuente") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType },
            ),
        ) { entrada ->
            val argumentos = requireNotNull(entrada.arguments)
            PersonajeDetailScreen(
                fuente = FuenteApi.valueOf(requireNotNull(argumentos.getString("fuente"))),
                id = argumentos.getInt("id"),
                onVolver = { navController.popBackStack() },
            )
        }
    }
}
