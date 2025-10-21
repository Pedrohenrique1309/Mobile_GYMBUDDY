package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class Publicacao(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("imagem")
    val imagem: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("data_publicacao")
    val dataPublicacao: String,
    
    @SerializedName("localizacao")
    val localizacao: String?,
    
    @SerializedName("curtidas_count")
    val curtidasCount: Int,
    
    @SerializedName("comentarios_count")
    val comentariosCount: Int,
    
    @SerializedName("id_user")
    val idUser: Int,
    
    @SerializedName("user")
    val user: List<Usuario>
)
