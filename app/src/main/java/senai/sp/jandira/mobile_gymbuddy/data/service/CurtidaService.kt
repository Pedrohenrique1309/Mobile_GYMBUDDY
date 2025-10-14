package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.*
import senai.sp.jandira.mobile_gymbuddy.data.model.*

interface CurtidaService {
    
    /**
     * Lista todas as curtidas
     * GET /v1/gymbuddy/curtida
     */
    @GET("v1/gymbuddy/curtida")
    suspend fun getAllCurtidas(): Response<CurtidaResponse>
    
    /**
     * Adiciona curtida em uma publicação
     * POST /v1/gymbuddy/curtida
     */
    @POST("v1/gymbuddy/curtida")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    suspend fun adicionarCurtida(
        @Body request: CurtidaRequest
    ): Response<Void>
    
    /**
     * Remove curtida (mesmo endpoint POST - API decide se adiciona ou remove)
     * POST /v1/gymbuddy/curtida
     */
    @POST("v1/gymbuddy/curtida")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    suspend fun toggleCurtida(
        @Body request: CurtidaRequest
    ): Response<Void>
}
