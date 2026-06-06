package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.domain.model.ImportConflict

@Composable
fun ImportConflictDialog(
    conflict: ImportConflict,
    onAddAsVersion: (versionName: String) -> Unit,
    onSaveAsSeparate: () -> Unit,
    onCancel: () -> Unit
) {
    var versionName by remember(conflict) { mutableStateOf(conflict.suggestedVersionName) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Ya tienes \"${conflict.existing.title}\"")
        },
        text = {
            Column {
                Text(text = "¿Qué quieres hacer?")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nombre de la versión:",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = versionName,
                    onValueChange = { versionName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAddAsVersion(versionName.trim()) },
                enabled = versionName.isNotBlank()
            ) {
                Text("Agregar como versión nueva")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onSaveAsSeparate) {
                    Text("Guardar como canción separada")
                }
                TextButton(onClick = onCancel) {
                    Text("Cancelar")
                }
            }
        }
    )
}
