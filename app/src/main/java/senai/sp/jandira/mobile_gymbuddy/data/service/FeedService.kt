package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.GET
import senai.sp.jandira.mobile_gymbuddy.data.model.FeedPublicacaoResponse

/**
 * Serviço otimizado para consumir o feed de publicações
 * Utiliza a VIEW vw_feed_publicacoes do banco de dados
 */
interface FeedService {
    
    /**
     * Busca o feed otimizado de publicações
     * Consome a view que já traz os dados JOINados (publicação + usuário)
     */
    // Backend disponibiliza a view via /v1/gymbuddy/view/feed
    @GET("v1/gymbuddy/view/feed")
    suspend fun getFeedPublicacoes(): Response<FeedPublicacaoResponse>
    
    /**
     * Busca publicações por localização
     * Aproveita a view otimizada com filtro de localização
     */
    // Filtragem por localizacao não implementada no backend; mantida assinatura para futuro suporte
    @GET("v1/gymbuddy/view/feed/localizacao/{localizacao}")
    suspend fun getFeedPorLocalizacao(
        @retrofit2.http.Path("localizacao") localizacao: String
    ): Response<FeedPublicacaoResponse>
}
