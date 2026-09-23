package com.organistaapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfUtils @Inject constructor(
    private val context: Context,
    private val ocrUtils: OcrUtils
) {
    suspend fun extrairTextoDePdf(uri: Uri): String = withContext(Dispatchers.IO) {
        val textos = mutableListOf<String>()

        val parcelFd: ParcelFileDescriptor = context.contentResolver
            .openFileDescriptor(uri, "r") ?: return@withContext ""

        parcelFd.use { fd ->
            PdfRenderer(fd).use { renderer ->
                val totalPaginas = renderer.pageCount
                for (i in 0 until totalPaginas) {
                    renderer.openPage(i).use { page ->
                        val escala = 2 // resolução 2x para melhor OCR
                        val largura = page.width * escala
                        val altura = page.height * escala
                        val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        val texto = ocrUtils.extrairTextoDeImagem(bitmap)
                        if (texto.isNotBlank()) textos.add(texto)

                        bitmap.recycle()
                    }
                }
            }
        }

        textos.joinToString("\n\n--- Página seguinte ---\n\n")
    }

    fun isPdf(uri: Uri): Boolean {
        val mime = context.contentResolver.getType(uri)
        return mime == "application/pdf" || uri.path?.endsWith(".pdf", ignoreCase = true) == true
    }
}
