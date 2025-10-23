package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo detalhado que corresponde à estrutura real da API
 */
data class NotificacaoDetalhada(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("id_curtida")
    val idCurtida: Int?,
    
    @SerializedName("tipo_notificacao")
    val tipoNotificacao: String,
    
    @SerializedName("data_criacao")
    val dataCriacao: String,
    
    @SerializedName("is_lida")
    val isLidaInt: Int,
    
    @SerializedName("usuario_destino")
    val usuarioDestino: List<UsuarioNotificacao>,
    
    @SerializedName("usuario_origem")
    val usuarioOrigem: List<UsuarioNotificacao>,
    
    @SerializedName("publicacao")
    val publicacao: List<PublicacaoNotificacao>?,
    
    @SerializedName("comentario")
    val comentario: List<ComentarioNotificacao>?
) {
    // Propriedade computed para converter Int em Boolean
    val isLida: Boolean
        get() = isLidaInt == 1
        
    // Propriedade computed para criar texto da notificação
    val textoNotificacao: String
        get() {
            val nomeOrigem = usuarioOrigem.firstOrNull()?.nickname ?: "Usuário"
            return when (tipoNotificacao) {
                "COMENTARIO" -> "$nomeOrigem comentou na sua publicação"
                "CURTIDA_PUBLICACAO" -> "$nomeOrigem curtiu sua publicação"
                "CURTIDA_COMENTARIO" -> "$nomeOrigem curtiu seu comentário"
                else -> "$nomeOrigem interagiu com você"
            }
        }
        
    // IDs para compatibilidade com código existente
    val idUsuarioDestino: Int
        get() = usuarioDestino.firstOrNull()?.id ?: 0
        
    val idUsuarioOrigem: Int
        get() = usuarioOrigem.firstOrNull()?.id ?: 0
        
    val nicknameOrigem: String
        get() = usuarioOrigem.firstOrNull()?.nickname ?: "user"
}

/**
 * Modelo simplificado para compatibilidade com código existente
 */
data class Notificacao(
    val id: Int,
    val idUsuarioDestino: Int,
    val idUsuarioOrigem: Int,
    val nicknameOrigem: String,
    val tipoNotificacao: String,
    val dataCriacao: String,
    val isLidaInt: Int,
    val idPublicacao: Int?,
    val idComentario: Int?,
    val textoNotificacao: String
) {
    val isLida: Boolean
        get() = isLidaInt == 1
}

/**
 * Modelos auxiliares para os arrays aninhados
 */
data class UsuarioNotificacao(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("nickname")
    val nickname: String,
    
    @SerializedName("foto")
    val foto: String?
)

data class PublicacaoNotificacao(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("imagem")
    val imagem: String?,
    
    @SerializedName("descricao")
    val descricao: String
)

data class ComentarioNotificacao(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("conteudo")
    val conteudo: String
)

/**
 * Response wrapper para a lista de notificações
 */
data class NotificacaoResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("itens")
    val itens: Int,
    
    @SerializedName("notificacoes")
    val notificacoes: List<NotificacaoDetalhada>?
)

/**
 * Modelo para retornar a contagem de notificações não lidas
 */
data class ContagemNotificacoes(
    @SerializedName("nao_lidas")
    val naoLidas: Int
)

/**
 * Enum para os tipos de notificação disponíveis
 */
enum class TipoNotificacao(val valor: String) {
    COMENTARIO("COMENTARIO"),
    CURTIDA_PUBLICACAO("CURTIDA_PUBLI"),
    CURTIDA_COMENTARIO("CURTIDA_COMEN")
}
