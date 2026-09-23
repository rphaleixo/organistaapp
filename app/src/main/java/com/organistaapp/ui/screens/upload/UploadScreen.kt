package com.organistaapp.ui.screens.upload

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.organistaapp.ui.screens.confirmacao.ConfirmacaoViewModel
import com.organistaapp.ui.theme.VioletaPrimario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onNavConfirmacao: () -> Unit,
    confirmacaoViewModel: ConfirmacaoViewModel,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val imagemLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.processarArquivo(it, it.lastPathSegment ?: "escala.jpg") }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.processarArquivo(it, it.lastPathSegment ?: "escala.pdf") }
    }
    val permissaoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) imagemLauncher.launch("image/*")
    }

    LaunchedEffect(uiState.uploadState) {
        val estado = uiState.uploadState
        if (estado is UploadState.ProntoParaConfirmar) {
            confirmacaoViewModel.carregarEventos(
                eventosExtraidos = estado.eventosExtraidos,
                textoEscala = estado.textoEscala,
                nomeArquivo = estado.nomeArquivo,
                caminhoArquivo = estado.caminhoArquivo
            )
            onNavConfirmacao()
            viewModel.resetar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar Escala") },
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
            when (val estado = uiState.uploadState) {
                is UploadState.Idle -> {
                    InstrucoesCard()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OpcaoUploadCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Image,
                            titulo = "Imagem",
                            subtitulo = "JPG, PNG",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissaoLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    imagemLauncher.launch("image/*")
                                }
                            }
                        )
                        OpcaoUploadCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.PictureAsPdf,
                            titulo = "PDF",
                            subtitulo = "Arquivo PDF",
                            onClick = { pdfLauncher.launch("application/pdf") }
                        )
                    }
                }

                is UploadState.Loading -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Lendo escala...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            uiState.arquivoSelecionado?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Extraindo texto e identificando dias de escala...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                            )
                        }
                    }
                }

                is UploadState.ProntoParaConfirmar -> {
                    // Transição ocorre via LaunchedEffect
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Abrindo tela de confirmação...")
                        }
                    }
                }

                is UploadState.Erro -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(estado.mensagem, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Button(
                        onClick = viewModel::resetar,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tentar novamente")
                    }
                }
            }
        }
    }
}

@Composable
private fun InstrucoesCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Como funciona", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "1" to "Envie a foto ou PDF da sua escala",
                "2" to "O app lê o texto e identifica seus dias",
                "3" to "Você revisa os eventos antes de salvar",
                "4" to "Eventos criados na agenda + notificações automáticas"
            ).forEach { (num, texto) ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(22.dp).clip(RoundedCornerShape(11.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(num, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(texto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun OpcaoUploadCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, VioletaPrimario.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(40.dp), tint = VioletaPrimario)
            Spacer(modifier = Modifier.height(8.dp))
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VioletaPrimario)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        }
    }
}
