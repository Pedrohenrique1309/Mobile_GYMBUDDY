package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import senai.sp.jandira.mobile_gymbuddy.data.model.Usuario

interface UsuarioService {

    @POST("/v1/gymbuddy/usuario")
    suspend fun cadastrarUsuario(@Body usuario: Usuario): Response<Void>
}