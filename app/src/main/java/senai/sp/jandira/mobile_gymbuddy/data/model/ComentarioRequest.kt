package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class ComentarioRequest(
    @SerializedName("conteudo")
    val conteudo: String,
    
    @SerializedName("data_comentario")
    val dataComentario: String,
    
    @SerializedName("id_publicacao")
    val idPublicacao: Int,
    
    @SerializedName("id_user")
    val idUser: Int
)
