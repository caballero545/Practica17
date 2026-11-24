package com.example.practica17.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practica17.data.Tarea
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// COMPONENTE CON ESTADO (Stateful)
@Composable
fun TareasScreen(
    viewModel: TareasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TareasContenido(
        uiState = uiState,
        onTextoCambio = viewModel::onTextoCambio,
        onAgregarTarea = viewModel::agregarTarea,
        onAlternarCompletada = viewModel::alternarCompletada,
        onEliminarTarea = viewModel::eliminarTarea,
        onCambiarFiltro = viewModel::cambiarFiltro,
        onLimpiarCompletadas = viewModel::limpiarCompletadas,
        onRecargar = { viewModel.cargarTareas(simularError = false) },
        onSimularError = { viewModel.cargarTareas(simularError = true) }
    )
}

// COMPONENTE SIN ESTADO (Stateless)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasContenido(
    uiState: TareasUiState,
    onTextoCambio: (String) -> Unit,
    onAgregarTarea: () -> Unit,
    onAlternarCompletada: (Int) -> Unit,
    onEliminarTarea: (Int) -> Unit,
    onCambiarFiltro: (FiltroTarea) -> Unit,
    onLimpiarCompletadas: () -> Unit,
    onRecargar: () -> Unit,
    onSimularError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MVVM - Gestor de Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onSimularError) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Simular error de carga"
                        )
                    }
                    IconButton(onClick = onRecargar) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar tareas"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val estado = uiState.estadoCarga) {
                is EstadoCarga.Cargando -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando tareas...", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                is EstadoCarga.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = estado.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = onRecargar) {
                            Text("Reintentar")
                        }
                    }
                }

                is EstadoCarga.Exito -> {

                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tarjeta de estadísticas derivadas (Reto)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "Total: ${uiState.totalTareas} | Pendientes: ${uiState.totalPendientes} | Completadas: ${uiState.totalCompletadas}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Campo de texto y botón para agregar nueva tarea
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.textoNuevaTarea,
                                onValueChange = onTextoCambio,
                                label = { Text("Nueva tarea") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            FilledIconButton(onClick = onAgregarTarea) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar tarea"
                                )
                            }
                        }

                        // Filtros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FiltroTarea.entries.forEach { filtro ->
                                FilterChip(
                                    selected = uiState.filtroActivo == filtro,
                                    onClick = { onCambiarFiltro(filtro) },
                                    label = { Text(filtro.etiqueta) }
                                )
                            }
                        }

                        // Lista de tareas filtradas
                        if (uiState.tareasFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay tareas en esta categoría.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            uiState.tareasFiltradas.forEach { tarea ->
                                TareaItem(
                                    tarea = tarea,
                                    onAlternar = { onAlternarCompletada(tarea.id) },
                                    onEliminar = { onEliminarTarea(tarea.id) }
                                )
                            }
                        }

                        // Botón secundario para limpiar completadas (Reto)
                        OutlinedButton(
                            onClick = onLimpiarCompletadas,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.totalCompletadas > 0
                        ) {
                            Text("Limpiar completadas")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TareaItem(
    tarea: Tarea,
    onAlternar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onAlternar() }
            )
            Text(
                text = tarea.titulo,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textDecoration = if (tarea.completada) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}