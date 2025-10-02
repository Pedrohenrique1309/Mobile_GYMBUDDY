// ARQUIVO: data/model/LoginResponse.kt

package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

// Esta classe representa o objeto JSON completo que a API retorna
data class LoginResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("usuario") val usuario: List<UsuarioResponse>?
)

// Esta classe representa o objeto de usuário DENTRO da resposta da API
data class UsuarioResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("peso") val peso: Double?,
    @SerializedName("altura") val altura: Double?,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("data_nascimento") val data_nascimento: String?,
    @SerializedName("foto_perfil") val foto_perfil: String?
)