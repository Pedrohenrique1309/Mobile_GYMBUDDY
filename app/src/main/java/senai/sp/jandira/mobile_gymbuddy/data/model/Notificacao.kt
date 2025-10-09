package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para consumir dados da VIEW vw_notificacoes_detalhadas
 * Esta view já traz as notificações formatadas e JOINado com dados do usuário
 */
data class Notificacao(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("id_usuario_destino")
    val idUsuarioDestino: Int,
    
    @SerializedName("id_usuario_origem")
    val idUsuarioOrigem: Int,
    
    @SerializedName("nickname_origem")
    val nicknameOrigem: String,
    
    @SerializedName("tipo_notificacao")
    val tipoNotificacao: String,
    
    @SerializedName("data_criacao")
    val dataCriacao: String,
    
    @SerializedName("is_lida")
    val isLida: Boolean,
    
    @SerializedName("id_publicacao")
    val idPublicacao: Int?,
    
    @SerializedName("id_comentario")
    val idComentario: Int?,
    
    @SerializedName("texto_notificacao")
    val textoNotificacao: String
)

/**
 * Response wrapper para a lista de notificações
 */
data class NotificacaoResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("notificacoes")
    val notificacoes: List<Notificacao>
)

/**
 * Enum para os tipos de notificação disponíveis
 */
enum class TipoNotificacao(val valor: String) {
    COMENTARIO("COMENTARIO"),
    CURTIDA_PUBLICACAO("CURTIDA_PUBLI"),
    CURTIDA_COMENTARIO("CURTIDA_COMEN")
}
