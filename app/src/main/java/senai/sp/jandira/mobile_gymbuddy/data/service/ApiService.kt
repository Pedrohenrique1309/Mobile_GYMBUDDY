package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.http.GET
import senai.sp.jandira.mobile_gymbuddy.data.model.ApiResponse

interface ApiService {
    // Define o endpoint que queremos chamar
    @GET("v1/gymbuddy/publicacao")
    suspend fun getPublicacoes(): ApiResponse
}