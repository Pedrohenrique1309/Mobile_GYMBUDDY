package senai.sp.jandira.mobile_gymbuddy.data.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import senai.sp.jandira.mobile_gymbuddy.data.service.UsuarioService
import senai.sp.jandira.mobile_gymbuddy.data.service.PublicacaoService
import senai.sp.jandira.mobile_gymbuddy.data.service.ComentarioService
import com.google.gson.GsonBuilder

object RetrofitFactory {

    // MODIFICADO AQUI: URL para acesso do emulador ao localhost do computador
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
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
}