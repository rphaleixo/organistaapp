package com.organistaapp.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.organistaapp.data.model.Evento
import com.organistaapp.ui.theme.DouradoAcento
import com.organistaapp.ui.theme.VioletaPrimario
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(viewModel: CalendarioViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendário de Escalas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Navegador de mês
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header do mês
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = viewModel::irParaMesAnterior) {
                                Icon(Icons.Filled.ChevronLeft, "Mês anterior")
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = uiState.mesSelecionado.month
                                        .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = uiState.mesSelecionado.year.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = viewModel::irParaProximoMes) {
                                Icon(Icons.Filled.ChevronRight, "Próximo mês")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grade do calendário
                        GradeCalendario(
                            mes = uiState.mesSelecionado,
                            eventos = uiState.eventosDoMes
                        )
                    }
                }
            }

            // Resumo do mês
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResumoCard(
                        modifier = Modifier.weight(1f),
                        numero = uiState.eventosDoMes.size.toString(),
                        label = "Escalas no mês"
                    )
                    val igrejas = uiState.eventosDoMes.map { it.nomeIgreja }.distinct()
                    ResumoCard(
                        modifier = Modifier.weight(1f),
                        numero = igrejas.size.toString(),
                        label = if (igrejas.size == 1) "Igreja" else "Igrejas"
                    )
                }
            }

            if (uiState.eventosDoMes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Sem escalas neste mês",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Dias de escala",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(uiState.eventosDoMes) { evento ->
                    EventoCalendarioItem(
                        evento = evento,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeCalendario(mes: YearMonth, eventos: List<Evento>) {
    val diasComEvento = eventos.map { evento ->
        Instant.ofEpochMilli(evento.dataHora)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .dayOfMonth
    }.toSet()

    val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    // Cabeçalho
    Row(modifier = Modifier.fillMaxWidth()) {
        diasSemana.forEach { dia ->
            Text(
                text = dia,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    val primeiroDia = mes.atDay(1)
    val diaSemanaInicio = (primeiroDia.dayOfWeek.value % 7)
    val totalDias = mes.lengthOfMonth()
    val hoje = LocalDate.now()

    val celulas = mutableListOf<Int?>()
    repeat(diaSemanaInicio) { celulas.add(null) }
    for (d in 1..totalDias) celulas.add(d)
    while (celulas.size % 7 != 0) celulas.add(null)

    celulas.chunked(7).forEach { semana ->
        Row(modifier = Modifier.fillMaxWidth()) {
            semana.forEach { dia ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (dia != null) {
                        val temEvento = dia in diasComEvento
                        val ehHoje = mes.atDay(dia) == hoje

                        val bgColor = when {
                            temEvento -> VioletaPrimario
                            ehHoje -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color.Transparent
                        }
                        val textColor = when {
                            temEvento -> Color.White
                            ehHoje -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dia.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor,
                                fontWeight = if (temEvento || ehHoje) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                        // Ponto indicador adicional
                        if (temEvento) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(DouradoAcento)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumoCard(modifier: Modifier = Modifier, numero: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EventoCalendarioItem(evento: Evento, modifier: Modifier = Modifier) {
    val dataHora = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(evento.dataHora),
        ZoneId.systemDefault()
    )
    val diaSemana = dataHora.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(VioletaPrimario)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Church,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = evento.nomeIgreja,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "dia ${dataHora.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VioletaPrimario
                )
                Text(
                    text = diaSemana,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = dataHora.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelMedium,
                    color = DouradoAcento
                )
            }
        }
    }
}
