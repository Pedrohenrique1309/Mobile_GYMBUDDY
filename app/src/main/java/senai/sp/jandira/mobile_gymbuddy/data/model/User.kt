package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("val_senha") val senha: String
)