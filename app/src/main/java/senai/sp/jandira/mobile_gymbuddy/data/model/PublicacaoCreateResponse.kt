package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class PublicacaoCreateResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("publicacao")
    val publicacao: Publicacao
)
