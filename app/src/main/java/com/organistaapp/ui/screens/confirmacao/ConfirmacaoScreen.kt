package com.organistaapp.ui.screens.confirmacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.organistaapp.ui.theme.VioletaPrimario
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacaoScreen(
    onSucesso: () -> Unit,
    onVoltar: () -> Unit,
    viewModel: ConfirmacaoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.sucesso) {
        if (uiState.sucesso) onSucesso()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Eventos") },
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
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val selecionados = uiState.eventos.count { it.incluir }
                    Text(
                        "$selecionados de ${uiState.eventos.size} evento(s) selecionados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    uiState.erro?.let {
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Button(
                        onClick = viewModel::confirmarESalvar,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.salvando && uiState.eventos.any { it.incluir }
                    ) {
                        if (uiState.salvando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Check, null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.salvando) "Salvando..." else "Salvar e agendar notificações")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Revise os eventos abaixo. Desmarque os que não quer salvar ou edite os detalhes.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            items(uiState.eventos, key = { it.id }) { evento ->
                EventoConfirmacaoCard(
                    evento = evento,
                    onToggle = { viewModel.toggleEvento(evento.id) },
                    onAtualizarIgreja = { viewModel.atualizarIgreja(evento.id, it) }
                )
            }
        }
    }
}

@Composable
private fun EventoConfirmacaoCard(
    evento: EventoEditavel,
    onToggle: () -> Unit,
    onAtualizarIgreja: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val diaSemana = evento.data.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }
    var editandoIgreja by remember { mutableStateOf(false) }
    var nomeIgrejaTemp by remember(evento.nomeIgreja) { mutableStateOf(evento.nomeIgreja) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (evento.incluir) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (evento.incluir) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = evento.incluir,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Data
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (evento.incluir) VioletaPrimario else Color.Gray,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = evento.data.format(formatter),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = diaSemana,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (evento.incluir) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textDecoration = if (!evento.incluir) TextDecoration.LineThrough else null
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Igreja
                if (editandoIgreja) {
                    OutlinedTextField(
                        value = nomeIgrejaTemp,
                        onValueChange = { nomeIgrejaTemp = it },
                        label = { Text("Igreja") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                onAtualizarIgreja(nomeIgrejaTemp)
                                editandoIgreja = false
                            }) {
                                Icon(Icons.Filled.Check, "Confirmar", tint = VioletaPrimario)
                            }
                        }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Church, null,
                            modifier = Modifier.size(14.dp),
                            tint = if (evento.incluir) MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                   else MaterialTheme.colorScheme.onSurface.copy(0.3f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = evento.nomeIgreja,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (evento.incluir) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textDecoration = if (!evento.incluir) TextDecoration.LineThrough else null
                        )
                        if (evento.incluir) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { editandoIgreja = true },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Filled.Edit, "Editar", modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Hora
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule, null,
                        modifier = Modifier.size(14.dp),
                        tint = if (evento.incluir) MaterialTheme.colorScheme.onSurface.copy(0.5f)
                               else MaterialTheme.colorScheme.onSurface.copy(0.3f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = evento.hora.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (evento.incluir) MaterialTheme.colorScheme.onSurface.copy(0.6f)
                               else MaterialTheme.colorScheme.onSurface.copy(0.3f),
                        textDecoration = if (!evento.incluir) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}
