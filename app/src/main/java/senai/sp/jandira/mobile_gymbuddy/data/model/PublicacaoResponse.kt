package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class PublicacaoResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("itens")
    val itens: Int,
    
    @SerializedName("publicacoes")
    val publicacoes: List<Publicacao>
)
