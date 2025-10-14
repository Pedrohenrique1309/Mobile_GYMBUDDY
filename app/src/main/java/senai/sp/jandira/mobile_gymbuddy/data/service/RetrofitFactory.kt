package senai.sp.jandira.mobile_gymbuddy.data.service

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import senai.sp.jandira.mobile_gymbuddy.data.service.PublicacaoService
import senai.sp.jandira.mobile_gymbuddy.data.service.ComentarioService
import com.google.gson.GsonBuilder

object RetrofitFactory {

    // URL para localhost - Se não funcionar, troque por seu IP local (ex: http://192.168.1.100:8080/)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor para garantir Content-Type correto e debug
    private val contentTypeInterceptor = Interceptor { chain ->
        val original = chain.request()
        
        // Log detalhado da requisição
        android.util.Log.d("RetrofitDebug", "=== REQUISIÇÃO ===")
        android.util.Log.d("RetrofitDebug", "URL: ${original.url}")
        android.util.Log.d("RetrofitDebug", "Método: ${original.method}")
        android.util.Log.d("RetrofitDebug", "Headers originais: ${original.headers}")
        
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Accept", "application/json")
        
        val request = requestBuilder.build()
        
        android.util.Log.d("RetrofitDebug", "Headers finais: ${request.headers}")
        
        val response = chain.proceed(request)
        
        android.util.Log.d("RetrofitDebug", "=== RESPOSTA ===")
        android.util.Log.d("RetrofitDebug", "Status: ${response.code}")
        android.util.Log.d("RetrofitDebug", "Headers resposta: ${response.headers}")
        
        response
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(contentTypeInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val gson = GsonBuilder()
        //.excludeFieldsWithoutExposeAnnotation()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(httpClient)
        .build()

    fun getUsuarioService(): UsuarioService {
        return retrofit.create(UsuarioService::class.java)
    }

    fun getPublicacaoService(): PublicacaoService {
        return retrofit.create(PublicacaoService::class.java)
    }

    fun getComentarioService(): ComentarioService {
        return retrofit.create(ComentarioService::class.java)
    }

    // ========== NOVOS SERVIÇOS OTIMIZADOS ==========

    fun getFeedService(): FeedService {
        return retrofit.create(FeedService::class.java)
    }

    fun getCurtidaService(): CurtidaService {
        return retrofit.create(CurtidaService::class.java)
    }

    fun getNotificacaoService(): NotificacaoService {
        return retrofit.create(NotificacaoService::class.java)
    }
}