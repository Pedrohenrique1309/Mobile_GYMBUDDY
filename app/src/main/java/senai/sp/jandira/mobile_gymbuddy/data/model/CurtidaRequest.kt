package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request para operações de curtida em publicações
 * Usado com os endpoints reais da API
 */
data class CurtidaPublicacaoRequest(
    @SerializedName("id_publicacao")
    val idPublicacao: Int,
    
    @SerializedName("id_user")
    val idUser: Int
)

/**
 * Modelo de curtida retornado pela API
 */
data class CurtidaItem(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("id_publicacao")
    val idPublicacao: Int,
    
    @SerializedName("id_user")
    val idUser: Int,
    
    @SerializedName("data_curtida")
    val dataCurtida: String? = null
)

/**
 * Request para operações de curtida em comentários
 * Usado com as procedures sp_adicionar_curtida_comentario e sp_remover_curtida_comentario
 */
data class CurtidaComentarioRequest(
    @SerializedName("id_comentario")
    val idComentario: Int,
    
    @SerializedName("id_user")
    val idUser: Int
)

/**
 * Response padrão para operações de curtida
 */
data class CurtidaResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("curtidas")
    val curtidas: List<CurtidaItem>? = null,
    
    @SerializedName("curtida")
    val curtida: CurtidaItem? = null,
    
    @SerializedName("curtidas_count")
    val curtidasCount: Int? = null
)

/**
 * Status da curtida de um usuário em uma publicação/comentário
 */
data class StatusCurtida(
    @SerializedName("isCurtido")
    val isCurtido: Boolean,
    
    @SerializedName("curtidas_count")
    val curtidasCount: Int
)
