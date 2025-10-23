// ARQUIVO: data/model/Usuario.kt

// GARANTA QUE ESTA É A PRIMEIRA LINHA DO ARQUIVO
package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

// Código limpo, sem o @Expose que não estava sendo usado
data class Usuario(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("peso") val peso: Double?,
    @SerializedName("altura") val altura: Double?,
    @SerializedName("imc") val imc: Double?,
    @SerializedName("data_nascimento") val dataNascimento: String?,
    @SerializedName("foto") val foto: String?,
    @SerializedName("descricao") val descricao: String?,
    @SerializedName("localizacao") val localizacao: String?,
    @SerializedName("is_bloqueado") val isBloqueado: Int
)