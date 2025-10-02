package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.GET
import senai.sp.jandira.mobile_gymbuddy.data.model.PublicacaoResponse

interface PublicacaoService {
    @GET("v1/gymbuddy/publicacao")
    suspend fun getPublicacoes(): Response<PublicacaoResponse>
}
