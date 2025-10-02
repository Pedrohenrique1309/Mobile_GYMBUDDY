// ARQUIVO: data/model/Usuario.kt

// GARANTA QUE ESTA É A PRIMEIRA LINHA DO ARQUIVO
package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

// Código limpo, sem o @Expose que não estava sendo usado
data class Usuario(
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String,
    @SerializedName("nickname") val nickname: String
)