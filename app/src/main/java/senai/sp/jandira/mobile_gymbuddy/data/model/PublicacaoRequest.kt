package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class PublicacaoRequest(
    @SerializedName("imagem")
    val imagem: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("localizacao")
    val localizacao: String?,
    
    @SerializedName("data")
    val data: String,
    
    @SerializedName("id_user")
    val idUser: Int
)
