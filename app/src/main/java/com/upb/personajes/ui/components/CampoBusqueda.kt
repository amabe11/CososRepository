package com.upb.personajes.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

/** Campo para buscar por nombre. Busca al pulsar la lupa del teclado. */
@Composable
fun CampoBusqueda(
    texto: String,
    onTextoCambia: (String) -> Unit,
    onBuscar: () -> Unit,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val foco = LocalFocusManager.current
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoCambia,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("Buscar por nombre") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onLimpiar(); foco.clearFocus() }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onBuscar(); foco.clearFocus() }),
    )
}
