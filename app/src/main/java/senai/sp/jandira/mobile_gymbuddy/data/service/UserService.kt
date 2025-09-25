// ARQUIVO: data/service/UsuarioService.kt

package senai.sp.jandira.mobile_gymbuddy.data.service

import senai.sp.jandira.mobile_gymbuddy.data.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import senai.sp.jandira.mobile_gymbuddy.data.model.LoginResponse
import retrofit2.http.Headers // <-- MUDANÇA 1: Importe o Headers

interface UsuarioService {

    // MUDANÇA 2: Adicione a anotação @Headers logo acima do @POST
    @Headers("Content-Type: application/json")
    @POST("v1/gymbuddy/usuario")
    suspend fun cadastrarUsuario(@Body usuario: Usuario): Response<Void>

    // A função de LOGIN continua a mesma, pois não envia 'body'
    @GET("v1/gymbuddy/usuario/login/email/senha")
    suspend fun logarUsuario(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Response<LoginResponse>
}