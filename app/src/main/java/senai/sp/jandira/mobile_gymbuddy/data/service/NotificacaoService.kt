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
    @GET("v1/gymbuddy/notificacao/usuario/{idUsuario}")
    suspend fun getNotificacoesUsuario(
        @retrofit2.http.Path("idUsuario") idUsuario: Int
    ): Response<NotificacaoResponse>
    
    /**
     * Busca todas as notificações usando a view detalhada
     */
    @GET("v1/gymbuddy/view/notificacoes")
    suspend fun getTodasNotificacoes(): Response<NotificacaoResponse>
    
    /**
     * Endpoint alternativo para testar se o básico funciona
     */
    @GET("v1/gymbuddy/notificacao")
    suspend fun getTodasNotificacoesBasico(): Response<NotificacaoResponse>
    
    /**
     * Busca apenas notificações não lidas de um usuário
     */
    // Endpoint para notificações não lidas não existe no backend atual.
    // Mantemos a assinatura para compatibilidade futura, mas o endpoint deve ser implementado no backend se necessário.
    @GET("v1/gymbuddy/notificacao/usuario/{idUsuario}/nao-lidas")
    suspend fun getNotificacoesNaoLidas(
        @retrofit2.http.Path("idUsuario") idUsuario: Int
    ): Response<NotificacaoResponse>
    
    /**
     * Marca uma notificação como lida
     */
    // Marcar como lida ainda não implementado no backend. Endpoint ajustado para path compatível quando adicionado.
    @PATCH("v1/gymbuddy/notificacao/{idNotificacao}/marcar-lida")
    suspend fun marcarComoLida(
        @retrofit2.http.Path("idNotificacao") idNotificacao: Int
    ): Response<Unit>
    
    /**
     * Marca todas as notificações de um usuário como lidas
     */
    @PATCH("v1/gymbuddy/notificacao/usuario/{idUsuario}/marcar-todas-lidas")
    suspend fun marcarTodasComoLidas(
        @retrofit2.http.Path("idUsuario") idUsuario: Int
    ): Response<Unit>
    
    /**
     * Conta o número de notificações não lidas
     */
    @GET("v1/gymbuddy/notificacao/usuario/{idUsuario}/count-nao-lidas")
    suspend fun contarNotificacoesNaoLidas(
        @retrofit2.http.Path("idUsuario") idUsuario: Int
    ): Response<senai.sp.jandira.mobile_gymbuddy.data.model.ContagemNotificacoes>
}
