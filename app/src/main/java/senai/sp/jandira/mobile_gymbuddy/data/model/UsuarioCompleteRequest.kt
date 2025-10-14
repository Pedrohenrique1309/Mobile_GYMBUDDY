package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo completo para cadastro de usuário com todos os dados
 */
data class UsuarioCompleteRequest(
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("email") 
    val email: String,
    
    @SerializedName("senha")
    val senha: String,
    
    @SerializedName("peso")
    val peso: Double,
    
    @SerializedName("altura")
    val altura: Double,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("data_nascimento")
    val dataNascimento: String?,
    
    @SerializedName("foto_perfil")
    val fotoPerfil: String?
)

/**
 * Resposta da API para cadastro de usuário
 */
data class UsuarioCreateResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("usuario")
    val usuario: Usuario
)
