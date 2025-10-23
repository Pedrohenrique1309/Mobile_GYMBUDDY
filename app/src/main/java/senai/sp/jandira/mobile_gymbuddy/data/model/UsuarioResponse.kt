package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para mapear a resposta do endpoint de busca de usuário por ID
 * GET /v1/gymbuddy/usuario/{id}
 */
data class UsuarioPerfilResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("Itens")
    val itens: Int,
    
    @SerializedName("usuario")
    val usuario: List<UsuarioDetalhes>
)

/**
 * Dados detalhados do usuário retornados pela API
 */
data class UsuarioDetalhes(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("senha")
    val senha: String,
    
    @SerializedName("peso")
    val peso: Double?,
    
    @SerializedName("altura")
    val altura: Double?,
    
    @SerializedName("imc")
    val imc: Double?,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("data_nascimento")
    val dataNascimento: String?,
    
    @SerializedName("foto")
    val foto: String?,
    
    @SerializedName("descricao")
    val descricao: String?,
    
    @SerializedName("localizacao")
    val localizacao: String?,
    
    @SerializedName("is_bloqueado")
    val isBloqueado: Int // 0 = false, 1 = true
)
