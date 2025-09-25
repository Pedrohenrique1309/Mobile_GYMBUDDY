package senai.sp.jandira.mobile_gymbuddy.data.service

import senai.sp.jandira.mobile_gymbuddy.data.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import senai.sp.jandira.mobile_gymbuddy.data.model.LoginResponse
import retrofit2.http.Headers

interface UsuarioService {
    @Headers("Content-Type: application/json")
    @POST("v1/gymbuddy/usuario")
    suspend fun cadastrarUsuario(@Body usuario: Usuario): Response<Void>
    @GET("v1/gymbuddy/usuario/login/email/senha")
    suspend fun logarUsuario(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Response<LoginResponse>
}