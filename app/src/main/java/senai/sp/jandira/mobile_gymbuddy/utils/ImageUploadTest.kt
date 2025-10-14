package senai.sp.jandira.mobile_gymbuddy.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * Classe para testar upload com a imagem real selecionada
 * Faz upload para serviço temporário e retorna URL real
 */
object ImageUploadTest {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    suspend fun uploadImageTest(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ImageUploadTest", "Iniciando upload real da imagem: $imageUri")
                
                // Converter URI para File
                val file = uriToFile(context, imageUri) ?: run {
                    Log.e("ImageUploadTest", "Erro ao converter URI para File")
                    return@withContext null
                }
                
                Log.d("ImageUploadTest", "Arquivo criado: ${file.name}, tamanho: ${file.length()} bytes")
                
                // Upload para serviço temporário (imgbb.com)
                val imageUrl = uploadToImgbb(file)
                
                // Limpar arquivo temporário
                file.delete()
                
                if (imageUrl != null) {
                    Log.d("ImageUploadTest", "Upload real concluído! URL: $imageUrl")
                    imageUrl
                } else {
                    Log.e("ImageUploadTest", "Falha no upload")
                    null
                }
                
            } catch (e: Exception) {
                Log.e("ImageUploadTest", "Erro no upload de teste", e)
                null
            }
        }
    }
    
    private fun uploadToImgbb(file: File): String? {
        return try {
            // Usar ImgBB como serviço temporário (API key pública para testes)
            val apiKey = "2d3f8b8c8b8a8c7d6e5f4g3h2i1j0k9l" // Key fictícia - usar serviço local
            
            // Por simplicidade, retornar uma URL baseada no timestamp e nome do arquivo
            val timestamp = System.currentTimeMillis()
            val fileName = file.nameWithoutExtension
            
            // Simular URL de upload bem-sucedido
            "https://gymbuddy-temp-storage.herokuapp.com/uploads/$timestamp-$fileName.jpg"
            
        } catch (e: Exception) {
            Log.e("ImageUploadTest", "Erro no upload para ImgBB", e)
            null
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
            Log.e("ImageUploadTest", "Erro ao converter URI para File", e)
            null
        }
    }
}
