package senai.sp.jandira.mobile_gymbuddy.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import senai.sp.jandira.mobile_gymbuddy.data.model.Notificacao
import senai.sp.jandira.mobile_gymbuddy.data.service.NotificacaoService
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory

/**
 * Repository para gerenciar operações relacionadas às notificações
 */
class NotificacaoRepository {
    
    private val notificacaoService = RetrofitFactory.getNotificacaoService()
    
    /**
     * Busca todas as notificações de um usuário específico
     */
    suspend fun getNotificacoesUsuario(idUsuario: Int): Flow<List<Notificacao>> = flow {
        try {
            val response = notificacaoService.getNotificacoesUsuario(idUsuario)
            if (response.isSuccessful && response.body()?.status == true) {
                emit(response.body()?.notificacoes ?: emptyList())
            } else {
                Log.e("NotificacaoRepository", "Erro ao buscar notificações: ${response.errorBody()?.string()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Exceção ao buscar notificações", e)
            emit(emptyList())
        }
    }
    
    /**
     * Busca todas as notificações usando a view detalhada
     * Endpoint: http://localhost:8080/v1/gymbuddy/view/notificacoes
     */
    suspend fun getTodasNotificacoes(): Flow<List<Notificacao>> = flow {
        try {
            val response = notificacaoService.getTodasNotificacoes()
            if (response.isSuccessful && response.body()?.status == true) {
                emit(response.body()?.notificacoes ?: emptyList())
            } else {
                Log.e("NotificacaoRepository", "Erro ao buscar todas notificações: ${response.errorBody()?.string()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Exceção ao buscar todas notificações", e)
            emit(emptyList())
        }
    }
    
    /**
     * Busca apenas notificações não lidas de um usuário
     */
    suspend fun getNotificacoesNaoLidas(idUsuario: Int): Flow<List<Notificacao>> = flow {
        try {
            val response = notificacaoService.getNotificacoesNaoLidas(idUsuario)
            if (response.isSuccessful && response.body()?.status == true) {
                emit(response.body()?.notificacoes ?: emptyList())
            } else {
                Log.e("NotificacaoRepository", "Erro ao buscar notificações não lidas: ${response.errorBody()?.string()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Exceção ao buscar notificações não lidas", e)
            emit(emptyList())
        }
    }
    
    /**
     * Marca uma notificação como lida
     */
    suspend fun marcarComoLida(idNotificacao: Int): Boolean {
        return try {
            val response = notificacaoService.marcarComoLida(idNotificacao)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Erro ao marcar notificação como lida", e)
            false
        }
    }
    
    /**
     * Marca todas as notificações de um usuário como lidas
     */
    suspend fun marcarTodasComoLidas(idUsuario: Int): Boolean {
        return try {
            val response = notificacaoService.marcarTodasComoLidas(idUsuario)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Erro ao marcar todas notificações como lidas", e)
            false
        }
    }
    
    /**
     * Conta o número de notificações não lidas
     */
    suspend fun contarNotificacoesNaoLidas(idUsuario: Int): Int {
        return try {
            val response = notificacaoService.contarNotificacoesNaoLidas(idUsuario)
            if (response.isSuccessful) {
                response.body()?.get("count") ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Erro ao contar notificações não lidas", e)
            0
        }
    }
}
