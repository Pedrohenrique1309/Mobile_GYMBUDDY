package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo otimizado para consumir dados da VIEW vw_feed_publicacoes
 * Esta view já traz os dados da publicação JOINado com dados do usuário
 */
data class FeedPublicacao(
    @SerializedName("id_publicacao")
    val idPublicacao: Int,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("imagem")
    val imagem: String,
    
    @SerializedName("data_publicacao")
    val dataPublicacao: String,
    
    @SerializedName("localizacao")
    val localizacao: String,
    
    @SerializedName("curtidas_count")
    val curtidasCount: Int,
    
    @SerializedName("comentarios_count")
    val comentariosCount: Int,
    
    @SerializedName("id_user")
    val idUser: Int,
    
    @SerializedName("nome_usuario")
    val nomeUsuario: String,
    
    @SerializedName("foto_perfil")
    val fotoPerfil: String?
)

/**
 * Response wrapper para a lista de publicações do feed
 */
data class FeedPublicacaoResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("feed")
    val feed: List<FeedPublicacao>
)
