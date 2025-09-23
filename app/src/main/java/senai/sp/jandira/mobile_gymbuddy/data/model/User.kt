package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Usuario(
    @Expose(serialize = true)
    @SerializedName("nome") val nome: String? = null,
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String,
    @SerializedName("nickname") val nickname: String
)