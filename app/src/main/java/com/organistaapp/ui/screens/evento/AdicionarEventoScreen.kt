package com.organistaapp.ui.screens.evento

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.organistaapp.data.model.Igreja
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarEventoScreen(
    onVoltar: () -> Unit,
    onSucesso: () -> Unit,
    viewModel: AdicionarEventoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.sucesso) {
        if (uiState.sucesso) onSucesso()
    }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarIgrejaDropdown by remember { mutableStateOf(false) }

    if (mostrarDatePicker) {
        DatePickerDialog(
            data = uiState.data,
            onDismiss = { mostrarDatePicker = false },
            onConfirm = { data ->
                viewModel.atualizarData(data)
                mostrarDatePicker = false
            }
        )
    }

    if (mostrarTimePicker) {
        TimePickerDialog(
            hora = uiState.hora,
            onDismiss = { mostrarTimePicker = false },
            onConfirm = { hora ->
                viewModel.atualizarHora(hora)
                mostrarTimePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Evento") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Igreja
            Text("Igreja", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

            if (uiState.igrejas.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = mostrarIgrejaDropdown,
                    onExpandedChange = { mostrarIgrejaDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.igrejaSelecionada?.nome ?: uiState.nomeIgrejaManual,
                        onValueChange = { viewModel.atualizarNomeIgreja(it) },
                        label = { Text("Nome da Igreja") },
                        leadingIcon = { Icon(Icons.Filled.Church, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mostrarIgrejaDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = mostrarIgrejaDropdown,
                        onDismissRequest = { mostrarIgrejaDropdown = false }
                    ) {
                        uiState.igrejas.forEach { igreja ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(igreja.nome, fontWeight = FontWeight.Medium)
                                        if (igreja.horarioPadrao.isNotBlank()) {
                                            Text(
                                                "Horário padrão: ${igreja.horarioPadrao}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selecionarIgreja(igreja)
                                    mostrarIgrejaDropdown = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Digitar nome manualmente...") },
                            onClick = {
                                viewModel.selecionarIgreja(null)
                                mostrarIgrejaDropdown = false
                            },
                            leadingIcon = { Icon(Icons.Filled.Edit, null) }
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.nomeIgrejaManual,
                    onValueChange = { viewModel.atualizarNomeIgreja(it) },
                    label = { Text("Nome da Igreja") },
                    leadingIcon = { Icon(Icons.Filled.Church, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Cadastre igrejas no menu Igrejas para facilitar.") }
                )
            }

            HorizontalDivider()

            // Data
            Text("Data", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            OutlinedCard(
                onClick = { mostrarDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.data.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                }
            }

            HorizontalDivider()

            // Hora
            Text("Horário", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            OutlinedCard(
                onClick = { mostrarTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.hora.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notificações info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Notificações automáticas serão agendadas: 72h antes e às 9h do dia.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            uiState.erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = viewModel::salvar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.salvando
            ) {
                if (uiState.salvando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.salvando) "Salvando..." else "Salvar evento")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    data: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = data.atStartOfDay()
            .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val novaData = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    onConfirm(novaData)
                }
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    hora: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = hora.hour,
        initialMinute = hora.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(state.hour, state.minute))
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        text = { TimePicker(state = state) }
    )
}
