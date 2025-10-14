package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import senai.sp.jandira.mobile_gymbuddy.data.model.ComentarioRequest
import senai.sp.jandira.mobile_gymbuddy.data.model.ComentarioResponse

interface ComentarioService {
    @GET("v1/gymbuddy/comentario")
    suspend fun getComentarios(@Query("id_publicacao") idPublicacao: Int): Response<ComentarioResponse>
    
    @GET("v1/gymbuddy/comentario")
    suspend fun getAllComentarios(): Response<ComentarioResponse>
    
    @POST("v1/gymbuddy/comentario")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    suspend fun criarComentario(@Body comentario: ComentarioRequest): Response<Void>
}
