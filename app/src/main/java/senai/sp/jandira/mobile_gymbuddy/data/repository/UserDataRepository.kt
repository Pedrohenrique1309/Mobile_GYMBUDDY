package senai.sp.jandira.mobile_gymbuddy.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository para gerenciar dados temporários do usuário durante o cadastro
 */
class UserDataRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("user_temp_data", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_NOME = "temp_nome"
        private const val KEY_EMAIL = "temp_email"
        private const val KEY_SENHA = "temp_senha"
        private const val KEY_NICKNAME = "temp_nickname"
        private const val KEY_DATA_NASCIMENTO = "temp_data_nascimento"
        private const val KEY_FOTO_PERFIL = "temp_foto_perfil"
        private const val KEY_HAS_TEMP_DATA = "has_temp_data"
    }
    
    /**
     * Salva os dados temporários do cadastro
     */
    fun saveTemporaryUserData(
        nome: String,
        email: String,
        senha: String,
        nickname: String,
        dataNascimento: String?,
        fotoPerfil: String?
    ) {
        prefs.edit()
            .putString(KEY_NOME, nome)
            .putString(KEY_EMAIL, email)
            .putString(KEY_SENHA, senha)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_DATA_NASCIMENTO, dataNascimento)
            .putString(KEY_FOTO_PERFIL, fotoPerfil)
            .putBoolean(KEY_HAS_TEMP_DATA, true)
            .apply()
    }
    
    /**
     * Recupera os dados temporários do cadastro
     */
    fun getTemporaryUserData(): TempUserData? {
        return if (prefs.getBoolean(KEY_HAS_TEMP_DATA, false)) {
            TempUserData(
                nome = prefs.getString(KEY_NOME, "") ?: "",
                email = prefs.getString(KEY_EMAIL, "") ?: "",
                senha = prefs.getString(KEY_SENHA, "") ?: "",
                nickname = prefs.getString(KEY_NICKNAME, "") ?: "",
                dataNascimento = prefs.getString(KEY_DATA_NASCIMENTO, null),
                fotoPerfil = prefs.getString(KEY_FOTO_PERFIL, null)
            )
        } else null
    }
    
    /**
     * Limpa os dados temporários após cadastro completo
     */
    fun clearTemporaryData() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Verifica se há dados temporários salvos
     */
    fun hasTemporaryData(): Boolean {
        return prefs.getBoolean(KEY_HAS_TEMP_DATA, false)
    }
}

/**
 * Data class para representar os dados temporários do usuário
 */
data class TempUserData(
    val nome: String,
    val email: String,
    val senha: String,
    val nickname: String,
    val dataNascimento: String?,
    val fotoPerfil: String?
)
