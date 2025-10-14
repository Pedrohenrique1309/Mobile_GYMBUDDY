package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import senai.sp.jandira.mobile_gymbuddy.data.model.PublicacaoCreateResponse
import senai.sp.jandira.mobile_gymbuddy.data.model.PublicacaoRequest
import senai.sp.jandira.mobile_gymbuddy.data.model.PublicacaoResponse

interface PublicacaoService {
    @GET("v1/gymbuddy/publicacao")
    suspend fun getPublicacoes(): Response<PublicacaoResponse>
    
    @POST("v1/gymbuddy/publicacao")
    suspend fun criarPublicacao(@Body publicacao: PublicacaoRequest): Response<PublicacaoCreateResponse>
}
