package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class ComentarioResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("status_code")
    val statusCode: Int,
    
    @SerializedName("itens")
    val itens: Int,
    
    @SerializedName("comentarios")
    val comentarios: List<ComentarioApi>
)
