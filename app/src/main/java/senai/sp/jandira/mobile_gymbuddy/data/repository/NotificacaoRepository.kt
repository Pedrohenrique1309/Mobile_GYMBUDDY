package senai.sp.jandira.mobile_gymbuddy.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import senai.sp.jandira.mobile_gymbuddy.data.model.Notificacao
import senai.sp.jandira.mobile_gymbuddy.data.model.NotificacaoResponse
import senai.sp.jandira.mobile_gymbuddy.data.model.ContagemNotificacoes
import senai.sp.jandira.mobile_gymbuddy.data.service.NotificacaoService
import senai.sp.jandira.mobile_gymbuddy.data.service.RetrofitFactory

/**
 * Repository para gerenciar operações relacionadas às notificações
 */
class NotificacaoRepository {
    
    private val notificacaoService: NotificacaoService = RetrofitFactory.getNotificacaoService()
    
    /**
     * Busca todas as notificações de um usuário específico
     */
    suspend fun getNotificacoesUsuario(idUsuario: Int): Flow<List<Notificacao>> = flow {
        try {
            val response = notificacaoService.getNotificacoesUsuario(idUsuario)
            if (response.isSuccessful && response.body()?.status == true) {
                val notificacoesDetalhadas = response.body()?.notificacoes ?: emptyList()
                val notificacoes = notificacoesDetalhadas.map { notifDetalhada ->
                    Notificacao(
                        id = notifDetalhada.id,
                        idUsuarioDestino = notifDetalhada.idUsuarioDestino,
                        idUsuarioOrigem = notifDetalhada.idUsuarioOrigem,
                        nicknameOrigem = notifDetalhada.nicknameOrigem,
                        tipoNotificacao = notifDetalhada.tipoNotificacao,
                        dataCriacao = notifDetalhada.dataCriacao,
                        isLidaInt = notifDetalhada.isLidaInt,
                        idPublicacao = notifDetalhada.publicacao?.firstOrNull()?.id,
                        idComentario = notifDetalhada.comentario?.firstOrNull()?.id,
                        textoNotificacao = notifDetalhada.textoNotificacao
                    )
                }
                emit(notificacoes)
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
            Log.d("NotificacaoRepository", "🔍 Tentando endpoint principal: /v1/gymbuddy/view/notificacoes")
            val response = notificacaoService.getTodasNotificacoes()
            Log.d("NotificacaoRepository", "📡 Resposta principal: ${response.isSuccessful}, código: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("NotificacaoRepository", "📋 Body status: ${body.status}")
                Log.d("NotificacaoRepository", "📋 Body itens: ${body.itens}")
                Log.d("NotificacaoRepository", "📋 Body notificacoes size: ${body.notificacoes?.size ?: 0}")
                
                if (body.status && body.notificacoes != null) {
                    // Converter NotificacaoDetalhada para Notificacao
                    val notificacoes = body.notificacoes.map { notifDetalhada ->
                        Notificacao(
                            id = notifDetalhada.id,
                            idUsuarioDestino = notifDetalhada.idUsuarioDestino,
                            idUsuarioOrigem = notifDetalhada.idUsuarioOrigem,
                            nicknameOrigem = notifDetalhada.nicknameOrigem,
                            tipoNotificacao = notifDetalhada.tipoNotificacao,
                            dataCriacao = notifDetalhada.dataCriacao,
                            isLidaInt = notifDetalhada.isLidaInt,
                            idPublicacao = notifDetalhada.publicacao?.firstOrNull()?.id,
                            idComentario = notifDetalhada.comentario?.firstOrNull()?.id,
                            textoNotificacao = notifDetalhada.textoNotificacao
                        )
                    }
                    Log.d("NotificacaoRepository", "✅ Notificações convertidas: ${notificacoes.size}")
                    emit(notificacoes)
                    return@flow
                }
            }
            
            // Se o endpoint principal não funcionou, tentar o básico
            Log.w("NotificacaoRepository", "⚠️ Endpoint principal falhou, tentando alternativo: /v1/gymbuddy/notificacao")
            val responseBasico = notificacaoService.getTodasNotificacoesBasico()
            Log.d("NotificacaoRepository", "📡 Resposta alternativa: ${responseBasico.isSuccessful}, código: ${responseBasico.code()}")
            
            if (responseBasico.isSuccessful && responseBasico.body() != null) {
                val body = responseBasico.body()!!
                if (body.status && body.notificacoes != null) {
                    // Converter NotificacaoDetalhada para Notificacao
                    val notificacoes = body.notificacoes.map { notifDetalhada ->
                        Notificacao(
                            id = notifDetalhada.id,
                            idUsuarioDestino = notifDetalhada.idUsuarioDestino,
                            idUsuarioOrigem = notifDetalhada.idUsuarioOrigem,
                            nicknameOrigem = notifDetalhada.nicknameOrigem,
                            tipoNotificacao = notifDetalhada.tipoNotificacao,
                            dataCriacao = notifDetalhada.dataCriacao,
                            isLidaInt = notifDetalhada.isLidaInt,
                            idPublicacao = notifDetalhada.publicacao?.firstOrNull()?.id,
                            idComentario = notifDetalhada.comentario?.firstOrNull()?.id,
                            textoNotificacao = notifDetalhada.textoNotificacao
                        )
                    }
                    Log.d("NotificacaoRepository", "✅ Notificações do endpoint alternativo: ${notificacoes.size}")
                    emit(notificacoes)
                    return@flow
                }
            }
            
            // Se ambos falharam
            Log.e("NotificacaoRepository", "❌ Ambos endpoints falharam")
            try {
                val errorBody = response.errorBody()?.string()
                Log.e("NotificacaoRepository", "Error body principal: $errorBody")
                val errorBodyBasico = responseBasico.errorBody()?.string()
                Log.e("NotificacaoRepository", "Error body alternativo: $errorBodyBasico")
            } catch (e2: Exception) {
                Log.e("NotificacaoRepository", "Erro ao ler error bodies", e2)
            }
            emit(emptyList())
            
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "💥 Exceção ao buscar todas notificações", e)
            Log.e("NotificacaoRepository", "Tipo da exceção: ${e.javaClass.simpleName}")
            Log.e("NotificacaoRepository", "Mensagem: ${e.message}")
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
                val notificacoesDetalhadas = response.body()?.notificacoes ?: emptyList()
                val notificacoes = notificacoesDetalhadas.map { notifDetalhada ->
                    Notificacao(
                        id = notifDetalhada.id,
                        idUsuarioDestino = notifDetalhada.idUsuarioDestino,
                        idUsuarioOrigem = notifDetalhada.idUsuarioOrigem,
                        nicknameOrigem = notifDetalhada.nicknameOrigem,
                        tipoNotificacao = notifDetalhada.tipoNotificacao,
                        dataCriacao = notifDetalhada.dataCriacao,
                        isLidaInt = notifDetalhada.isLidaInt,
                        idPublicacao = notifDetalhada.publicacao?.firstOrNull()?.id,
                        idComentario = notifDetalhada.comentario?.firstOrNull()?.id,
                        textoNotificacao = notifDetalhada.textoNotificacao
                    )
                }
                emit(notificacoes)
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
                // agora o endpoint retorna um objeto ContagemNotificacoes com campo `nao_lidas`
                response.body()?.naoLidas ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("NotificacaoRepository", "Erro ao contar notificações não lidas", e)
            0
        }
    }
}
