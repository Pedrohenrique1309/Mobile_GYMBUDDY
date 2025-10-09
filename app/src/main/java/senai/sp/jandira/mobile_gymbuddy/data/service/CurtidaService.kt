package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.*
import senai.sp.jandira.mobile_gymbuddy.data.model.*

/**
 * Serviço para operações de curtida
 * Utiliza os endpoints reais da API
 */
interface CurtidaService {
    
    // ========== CURTIDAS EM PUBLICAÇÕES ==========
    
    /**
     * Adiciona curtida em uma publicação
     * POST /v1/gymbuddy/curtida
     */
    @POST("v1/gymbuddy/curtida")
    suspend fun inserirCurtida(
        @Body request: CurtidaPublicacaoRequest
    ): Response<CurtidaResponse>
    
    /**
     * Remove curtida por ID
     * DELETE /v1/gymbuddy/curtida/{search_id}
     */
    @DELETE("v1/gymbuddy/curtida/{search_id}")
    suspend fun excluirCurtida(
        @Path("search_id") curtidaId: Int
    ): Response<CurtidaResponse>
    
    /**
     * Lista todas as curtidas
     * GET /v1/gymbuddy/curtida
     */
    @GET("v1/gymbuddy/curtida")
    suspend fun listarCurtida(): Response<CurtidaResponse>
    
    /**
     * Busca curtida específica por ID
     * GET /v1/gymbuddy/curtida/{search_id}
     */
    @GET("v1/gymbuddy/curtida/{search_id}")
    suspend fun buscarCurtida(
        @Path("search_id") curtidaId: Int
    ): Response<CurtidaResponse>
    
    // ========== CURTIDAS EM COMENTÁRIOS ==========
    
    /**
     * Adiciona curtida em um comentário
     * Usa a procedure sp_adicionar_curtida_comentario
     */
    @POST("v1/gymbuddy/curtida/comentario/adicionar")
    suspend fun adicionarCurtidaComentario(
        @Body request: CurtidaComentarioRequest
    ): Response<CurtidaResponse>
    
    /**
     * Remove curtida de um comentário
     * Usa a procedure sp_remover_curtida_comentario
     */
    @POST("v1/gymbuddy/curtida/comentario/remover")
    suspend fun removerCurtidaComentario(
        @Body request: CurtidaComentarioRequest
    ): Response<CurtidaResponse>
    
    /**
     * Verifica se o usuário curtiu um comentário
     */
    @GET("v1/gymbuddy/curtida/comentario/{idComentario}/usuario/{idUser}")
    suspend fun verificarCurtidaComentario(
        @Path("idComentario") idComentario: Int,
        @Path("idUser") idUser: Int
    ): Response<StatusCurtida>
}
