package senai.sp.jandira.mobile_gymbuddy.utils

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * Classe para testar upload sem Azure
 * Simula upload e retorna URL de exemplo
 */
object ImageUploadTest {
    
    suspend fun uploadImageTest(context: Context, imageUri: Uri): String? {
        return try {
            Log.d("ImageUploadTest", "Simulando upload da imagem: $imageUri")
            
            // Simular delay de upload
            kotlinx.coroutines.delay(2000)
            
            // Retornar URL de exemplo para teste
            val testUrl = "https://tm.ibxk.com.br/materias/5866/21577.jpg?ims=fit-in/800x500/filters:quality(70)"
            
            Log.d("ImageUploadTest", "Upload simulado concluído! URL: $testUrl")
            testUrl
            
        } catch (e: Exception) {
            Log.e("ImageUploadTest", "Erro no upload de teste", e)
            null
        }
    }
}
