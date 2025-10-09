package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.*
import senai.sp.jandira.mobile_gymbuddy.data.model.NotificacaoResponse

/**
 * Serviço para o sistema de notificações
 * Utiliza a VIEW vw_notificacoes_detalhadas que é populada automaticamente pelas TRIGGERS
 */
interface NotificacaoService {
    
    /**
     * Busca todas as notificações de um usuário
     * Consome a view vw_notificacoes_detalhadas
     */
    @GET("v1/gymbuddy/notificacoes/usuario/{idUsuario}")
    suspend fun getNotificacoesUsuario(
        @Path("idUsuario") idUsuario: Int
    ): Response<NotificacaoResponse>
    
    /**
     * Busca apenas notificações não lidas de um usuário
     */
    @GET("v1/gymbuddy/notificacoes/usuario/{idUsuario}/nao-lidas")
    suspend fun getNotificacoesNaoLidas(
        @Path("idUsuario") idUsuario: Int
    ): Response<NotificacaoResponse>
    
    /**
     * Marca uma notificação como lida
     */
    @PATCH("v1/gymbuddy/notificacoes/{idNotificacao}/marcar-lida")
    suspend fun marcarComoLida(
        @Path("idNotificacao") idNotificacao: Int
    ): Response<Unit>
    
    /**
     * Marca todas as notificações de um usuário como lidas
     */
    @PATCH("v1/gymbuddy/notificacoes/usuario/{idUsuario}/marcar-todas-lidas")
    suspend fun marcarTodasComoLidas(
        @Path("idUsuario") idUsuario: Int
    ): Response<Unit>
    
    /**
     * Conta o número de notificações não lidas
     */
    @GET("v1/gymbuddy/notificacoes/usuario/{idUsuario}/count-nao-lidas")
    suspend fun contarNotificacoesNaoLidas(
        @Path("idUsuario") idUsuario: Int
    ): Response<Map<String, Int>>
}
