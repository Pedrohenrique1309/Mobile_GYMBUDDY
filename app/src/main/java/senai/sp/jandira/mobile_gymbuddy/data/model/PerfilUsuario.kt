package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para consumir dados da VIEW vw_perfil_publicacoes
 * Esta view traz o perfil completo do usuário com suas publicações
 */
data class PerfilUsuario(
    @SerializedName("id_user")
    val idUser: Int,
    
    @SerializedName("nome_usuario")
    val nomeUsuario: String,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("foto")
    val foto: String?,
    
    @SerializedName("descricao")
    val descricao: String?,
    
    @SerializedName("data_nascimento")
    val dataNascimento: String?,
    
    @SerializedName("localizacao")
    val localizacao: String?,
    
    @SerializedName("imc")
    val imc: Double?,
    
    @SerializedName("is_bloqueado")
    val isBloqueado: Boolean,
    
    // Dados da publicação (podem ser null se o usuário não tiver publicações)
    @SerializedName("id_publicacao")
    val idPublicacao: Int?,
    
    @SerializedName("foto_publicada")
    val fotoPublicada: String?,
    
    @SerializedName("descricao_publicacao")
    val descricaoPublicacao: String?,
    
    @SerializedName("data_publicacao")
    val dataPublicacao: String?,
    
    @SerializedName("curtidas_count")
    val curtidasCount: Int?,
    
    @SerializedName("comentarios_count")
    val comentariosCount: Int?
)

/**
 * Response wrapper para o perfil do usuário
 */
data class PerfilUsuarioResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("usuario")
    val usuario: PerfilUsuario?,
    
    @SerializedName("publicacoes")
    val publicacoes: List<PerfilUsuario>
)

/**
 * Modelo simplificado para exibir dados básicos do usuário
 * Utiliza a function fn_classificar_imc automaticamente
 */
data class UsuarioPerfil(
    val id: Int,
    val nome: String,
    val nickname: String,
    val foto: String?,
    val descricao: String?,
    val imc: Double?,
    val classificacaoImc: String, // Será calculado pela function do banco
    val totalPublicacoes: Int
)
