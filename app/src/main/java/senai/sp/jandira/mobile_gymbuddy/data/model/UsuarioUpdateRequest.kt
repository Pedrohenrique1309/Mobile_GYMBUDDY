package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para requisição de atualização de usuário
 * PUT /v1/gymbuddy/usuario/{id}
 */
data class UsuarioUpdateRequest(
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
    
    @SerializedName("imc")
    val imc: Double,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("data_nascimento")
    val dataNascimento: String?,
    
    @SerializedName("foto")
    val foto: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("localizacao")
    val localizacao: String,
    
    @SerializedName("is_bloqueado")
    val isBloqueado: Int
)

/**
 * Modelo para resposta de atualização de usuário
 */
data class UsuarioUpdateResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("item")
    val item: Boolean
)
