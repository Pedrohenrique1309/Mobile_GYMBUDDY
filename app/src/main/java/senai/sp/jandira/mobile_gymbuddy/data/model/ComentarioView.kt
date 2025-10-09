package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo otimizado para consumir dados da VIEW vw_comentarios_publicacao
 * Esta view já traz os dados do comentário JOINado com dados do usuário
 */
data class ComentarioView(
    @SerializedName("id_comentarios")
    val idComentarios: Int,
    
    @SerializedName("id_publicacao")
    val idPublicacao: Int,
    
    @SerializedName("conteudo_comentario")
    val conteudoComentario: String,
    
    @SerializedName("data_comentario")
    val dataComentario: String,
    
    @SerializedName("curtidas_count")
    val curtidasCount: Int,
    
    @SerializedName("id_user")
    val idUser: Int,
    
    @SerializedName("nome_usuario")
    val nomeUsuario: String,
    
    @SerializedName("foto_perfil")
    val fotoPerfil: String?
)

/**
 * Response wrapper para a lista de comentários usando a view
 */
data class ComentarioViewResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("comentarios")
    val comentarios: List<ComentarioView>
)
