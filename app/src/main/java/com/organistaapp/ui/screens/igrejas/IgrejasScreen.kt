package com.organistaapp.ui.screens.igrejas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

private val DIAS_SEMANA = listOf(
    "Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IgrejasScreen(
    viewModel: IgrejasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialog by remember { mutableStateOf(false) }
    var igrejaEditando by remember { mutableStateOf<Igreja?>(null) }
    var igrejaParaArquivar by remember { mutableStateOf<Igreja?>(null) }
    var igrejaParaDeletar by remember { mutableStateOf<Igreja?>(null) }
    var mostrarArquivadas by remember { mutableStateOf(false) }

    if (mostrarDialog || igrejaEditando != null) {
        IgrejaDialog(
            igreja = igrejaEditando,
            onDismiss = { mostrarDialog = false; igrejaEditando = null },
            onSalvar = { nome, endereco, dia, horario, rodizio ->
                if (igrejaEditando != null) {
                    viewModel.atualizarIgreja(igrejaEditando!!, nome, endereco, dia, horario, rodizio)
                } else {
                    viewModel.salvarIgreja(nome, endereco, dia, horario, rodizio)
                }
                mostrarDialog = false
                igrejaEditando = null
            }
        )
    }

    igrejaParaArquivar?.let { igreja ->
        AlertDialog(
            onDismissRequest = { igrejaParaArquivar = null },
            icon = { Icon(Icons.Filled.Archive, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Arquivar igreja") },
            text = {
                Text("\"${igreja.nome}\" será arquivada e não aparecerá nas listas. Você pode restaurá-la a qualquer momento.")
            },
            confirmButton = {
                Button(onClick = { viewModel.arquivarIgreja(igreja); igrejaParaArquivar = null }) {
                    Text("Arquivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { igrejaParaArquivar = null }) { Text("Cancelar") }
            }
        )
    }

    igrejaParaDeletar?.let { igreja ->
        AlertDialog(
            onDismissRequest = { igrejaParaDeletar = null },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir permanentemente") },
            text = {
                Text("\"${igreja.nome}\" será excluída para sempre. Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deletarIgreja(igreja); igrejaParaDeletar = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { igrejaParaDeletar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Igrejas") },
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

        val temIgrejas = uiState.igrejas.isNotEmpty()
        val temArquivadas = uiState.igrejasArquivadas.isNotEmpty()

        if (!temIgrejas && !temArquivadas) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Church, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Nenhuma igreja cadastrada",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "Toque no + para adicionar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Header com contagem
            if (temIgrejas) {
                item {
                    Text(
                        text = "${uiState.igrejas.size} ${if (uiState.igrejas.size == 1) "igreja ativa" else "igrejas ativas"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                items(uiState.igrejas, key = { it.id }) { igreja ->
                    IgrejaCard(
                        igreja = igreja,
                        arquivada = false,
                        onEditar = { igrejaEditando = igreja },
                        onArquivar = { igrejaParaArquivar = igreja },
                        onDeletar = null,
                        onDesarquivar = null
                    )
                }
            }

            // Seção de arquivadas
            if (temArquivadas) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Archive, null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Arquivadas (${uiState.igrejasArquivadas.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { mostrarArquivadas = !mostrarArquivadas }) {
                            Text(if (mostrarArquivadas) "Ocultar" else "Ver")
                        }
                    }
                }

                if (mostrarArquivadas) {
                    items(uiState.igrejasArquivadas, key = { "arq_${it.id}" }) { igreja ->
                        IgrejaCard(
                            igreja = igreja,
                            arquivada = true,
                            onEditar = null,
                            onArquivar = null,
                            onDeletar = { igrejaParaDeletar = igreja },
                            onDesarquivar = { viewModel.desarquivarIgreja(igreja) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IgrejaCard(
    igreja: Igreja,
    arquivada: Boolean,
    onEditar: (() -> Unit)?,
    onArquivar: (() -> Unit)?,
    onDeletar: (() -> Unit)?,
    onDesarquivar: (() -> Unit)?
) {
    val alpha = if (arquivada) 0.55f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (arquivada) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (arquivada)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar com inicial
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VioletaPrimario.copy(alpha = alpha)),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = igreja.nome,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha)
                        )
                        if (igreja.emRodizio) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    "Rodízio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (igreja.endereco.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.LocationOn, null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.5f * alpha)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                igreja.endereco,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f * alpha)
                            )
                        }
                    }
                    if (igreja.diaCulto.isNotBlank() || igreja.horarioPadrao.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.Schedule, null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.5f * alpha)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            val culto = listOf(igreja.diaCulto, igreja.horarioPadrao)
                                .filter { it.isNotBlank() }.joinToString(" · ")
                            Text(
                                culto,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f * alpha)
                            )
                        }
                    }
                }

                // Ações
                if (!arquivada) {
                    IconButton(onClick = { onEditar?.invoke() }) {
                        Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onArquivar?.invoke() }) {
                        Icon(
                            Icons.Filled.Archive, "Arquivar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                } else {
                    IconButton(onClick = { onDesarquivar?.invoke() }) {
                        Icon(
                            Icons.Filled.Unarchive, "Restaurar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDeletar?.invoke() }) {
                        Icon(
                            Icons.Filled.DeleteForever, "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IgrejaDialog(
    igreja: Igreja?,
    onDismiss: () -> Unit,
    onSalvar: (String, String, String, String, Boolean) -> Unit
) {
    var nome by remember { mutableStateOf(igreja?.nome ?: "") }
    var endereco by remember { mutableStateOf(igreja?.endereco ?: "") }
    var diaCulto by remember { mutableStateOf(igreja?.diaCulto ?: "") }
    var horario by remember { mutableStateOf(igreja?.horarioPadrao ?: "08:00") }
    var emRodizio by remember { mutableStateOf(igreja?.emRodizio ?: false) }
    var expandirDia by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (igreja == null) "Nova Igreja" else "Editar Igreja") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nome
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Igreja *") },
                    leadingIcon = { Icon(Icons.Filled.Church, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Endereço
                OutlinedTextField(
                    value = endereco,
                    onValueChange = { endereco = it },
                    label = { Text("Endereço") },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Dia do culto
                ExposedDropdownMenuBox(
                    expanded = expandirDia,
                    onExpandedChange = { expandirDia = it }
                ) {
                    OutlinedTextField(
                        value = diaCulto,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dia do culto") },
                        leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDia) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Selecione...") }
                    )
                    ExposedDropdownMenu(
                        expanded = expandirDia,
                        onDismissRequest = { expandirDia = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhum") },
                            onClick = { diaCulto = ""; expandirDia = false }
                        )
                        DIAS_SEMANA.forEach { dia ->
                            DropdownMenuItem(
                                text = { Text(dia) },
                                onClick = { diaCulto = dia; expandirDia = false },
                                trailingIcon = if (diaCulto == dia) {
                                    { Icon(Icons.Filled.Check, null, tint = VioletaPrimario) }
                                } else null
                            )
                        }
                    }
                }

                // Hora do culto
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Hora do culto (HH:mm)") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("08:00") }
                )

                // Switch rodízio
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (emRodizio)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Autorenew, null,
                            tint = if (emRodizio) MaterialTheme.colorScheme.secondary
                                   else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Faz parte do rodízio",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Esta igreja entra na escala de rodízio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        }
                        Switch(
                            checked = emRodizio,
                            onCheckedChange = { emRodizio = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nome.isNotBlank()) onSalvar(nome, endereco, diaCulto, horario, emRodizio) },
                enabled = nome.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
