package senai.sp.jandira.mobile_gymbuddy.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object AzureBlobUploader {
    
    private const val STORAGE_ACCOUNT = "gymbuddystorage"
    private const val CONTAINER_NAME = "fotos"
    private const val SAS_TOKEN = "sp=acw&st=2025-10-14T14:56:35Z&se=2025-10-14T23:11:35Z&sv=2024-11-04&sr=c&sig=Ns2XcPokYRGKxN3HEagkTi%2F3lUHnwAUtWF7tTwa1uRk%3D"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Faz upload de uma imagem para o Azure Blob Storage
     * @param context Contexto do Android
     * @param imageUri URI da imagem selecionada
     * @return URL da imagem uploadada ou null em caso de erro
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AzureUpload", "Iniciando upload da imagem: $imageUri")
                
                // Converter URI para File
                val file = uriToFile(context, imageUri) ?: run {
                    Log.e("AzureUpload", "Erro ao converter URI para File")
                    return@withContext null
                }
                
                Log.d("AzureUpload", "Arquivo criado: ${file.name}, tamanho: ${file.length()} bytes")
                
                // Gerar nome único para o blob
                val blobName = "${System.currentTimeMillis()}-${file.name}"
                val baseUrl = "https://$STORAGE_ACCOUNT.blob.core.windows.net/$CONTAINER_NAME/$blobName"
                val uploadUrl = "$baseUrl?$SAS_TOKEN"
                
                Log.d("AzureUpload", "URL de upload: $uploadUrl")
                
                // Determiner o tipo de conteúdo
                val mediaType = getMediaType(file)
                val requestBody = file.asRequestBody(mediaType)
                
                Log.d("AzureUpload", "Tipo de mídia: $mediaType")
                
                // Criar requisição
                val request = Request.Builder()
                    .url(uploadUrl)
                    .put(requestBody)
                    .addHeader("x-ms-blob-type", "BlockBlob")
                    .addHeader("Content-Type", mediaType.toString())
                    .build()
                
                // Executar upload
                Log.d("AzureUpload", "Executando upload...")
                val response = client.newCall(request).execute()
                
                Log.d("AzureUpload", "Resposta: ${response.code} - ${response.message}")
                
                // Limpar arquivo temporário
                file.delete()
                
                if (response.isSuccessful) {
                    Log.d("AzureUpload", "Upload bem-sucedido! URL: $baseUrl")
                    baseUrl
                } else {
                    Log.e("AzureUpload", "Erro no upload: ${response.code} - ${response.body?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("AzureUpload", "Exceção durante upload", e)
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Converte URI para File temporário
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Determina o tipo de mídia baseado na extensão do arquivo
     */
    private fun getMediaType(file: File): okhttp3.MediaType? {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg".toMediaTypeOrNull()
            "png" -> "image/png".toMediaTypeOrNull()
            "gif" -> "image/gif".toMediaTypeOrNull()
            "webp" -> "image/webp".toMediaTypeOrNull()
            else -> "application/octet-stream".toMediaTypeOrNull()
        }
    }
}
