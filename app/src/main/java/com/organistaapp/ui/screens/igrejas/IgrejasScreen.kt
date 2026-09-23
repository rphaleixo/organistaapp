package com.organistaapp.ui.screens.igrejas

import androidx.compose.foundation.background
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
import com.organistaapp.data.model.Igreja
import com.organistaapp.ui.theme.VioletaPrimario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IgrejasScreen(
    onVoltar: () -> Unit,
    viewModel: IgrejasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialog by remember { mutableStateOf(false) }
    var igrejaEditando by remember { mutableStateOf<Igreja?>(null) }
    var mostrarConfirmacaoDelete by remember { mutableStateOf<Igreja?>(null) }

    if (mostrarDialog || igrejaEditando != null) {
        IgrejaDialog(
            igreja = igrejaEditando,
            onDismiss = { mostrarDialog = false; igrejaEditando = null },
            onSalvar = { nome, endereco, horario ->
                if (igrejaEditando != null) {
                    viewModel.atualizarIgreja(igrejaEditando!!, nome, endereco, horario)
                } else {
                    viewModel.salvarIgreja(nome, endereco, horario)
                }
                mostrarDialog = false
                igrejaEditando = null
            }
        )
    }

    mostrarConfirmacaoDelete?.let { igreja ->
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoDelete = null },
            title = { Text("Remover igreja") },
            text = { Text("Deseja remover \"${igreja.nome}\"? Os eventos já cadastrados não serão excluídos.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deletarIgreja(igreja); mostrarConfirmacaoDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacaoDelete = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Igrejas") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Adicionar", tint = Color.White)
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.igrejas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Church, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Nenhuma igreja cadastrada", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Toque no + para adicionar", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.igrejas, key = { it.id }) { igreja ->
                IgrejaCard(
                    igreja = igreja,
                    onEditar = { igrejaEditando = igreja },
                    onDeletar = { mostrarConfirmacaoDelete = igreja }
                )
            }
        }
    }
}

@Composable
private fun IgrejaCard(
    igreja: Igreja,
    onEditar: () -> Unit,
    onDeletar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VioletaPrimario),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = igreja.nome.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = igreja.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (igreja.endereco.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(igreja.endereco, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Horário padrão: ${igreja.horarioPadrao}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDeletar) {
                Icon(Icons.Filled.Delete, "Deletar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun IgrejaDialog(
    igreja: Igreja?,
    onDismiss: () -> Unit,
    onSalvar: (String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf(igreja?.nome ?: "") }
    var endereco by remember { mutableStateOf(igreja?.endereco ?: "") }
    var horario by remember { mutableStateOf(igreja?.horarioPadrao ?: "08:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (igreja == null) "Nova Igreja" else "Editar Igreja") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Igreja") },
                    leadingIcon = { Icon(Icons.Filled.Church, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endereco,
                    onValueChange = { endereco = it },
                    label = { Text("Endereço (opcional)") },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Horário padrão (HH:mm)") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("08:00") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nome.isNotBlank()) onSalvar(nome, endereco, horario) },
                enabled = nome.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
