package senai.sp.jandira.mobile_gymbuddy.data.service

import Usuario
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface UsuarioService {
    @POST("v1/gymbuddy/usuario")
    @Headers("Content-Type: application/json") // força apenas application/json
    suspend fun cadastrarUsuario(@Body usuario: Usuario): Response<Usuario>
}
