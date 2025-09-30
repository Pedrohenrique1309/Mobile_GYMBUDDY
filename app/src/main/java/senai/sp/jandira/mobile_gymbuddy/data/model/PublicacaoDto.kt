package senai.sp.jandira.mobile_gymbuddy.data.model

import com.google.gson.annotations.SerializedName

// Classe que representa a resposta completa da API
data class ApiResponse(
    val status: Boolean,
    @SerializedName("status_code") val statusCode: Int,
    val itens: Int,
    val publicacoes: List<PublicacaoDto>
)

// Classe que representa uma única publicação da API
data class PublicacaoDto(
    val id: Int,
    val imagem: String,
    val descricao: String,
    @SerializedName("data_publicacao") val dataPublicacao: String,
    val localizacao: String?, // Pode ser nulo
    @SerializedName("curtidas_count") val curtidasCount: Int,
    @SerializedName("comentarios_count") val comentariosCount: Int,
    @SerializedName("id_user") val idUser: Int
)