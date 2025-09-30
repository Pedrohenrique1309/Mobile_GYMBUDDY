package senai.sp.jandira.mobile_gymbuddy.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT // <-- ADIÇÃO 1: Importar @PUT
import retrofit2.http.Path // <-- ADIÇÃO 2: Importar @Path
import retrofit2.http.Query
import senai.sp.jandira.mobile_gymbuddy.data.model.LoginResponse
import senai.sp.jandira.mobile_gymbuddy.data.model.Usuario
import senai.sp.jandira.mobile_gymbuddy.data.model.UsuarioUpdateRequest // <-- ADIÇÃO 3: Importar a nova data class

interface UsuarioService {
    @Headers("Content-Type: application/json")
    @POST("v1/gymbuddy/usuario")
    suspend fun cadastrarUsuario(@Body usuario: Usuario): Response<Void>

    @GET("v1/gymbuddy/usuario/login/email/senha")
    suspend fun logarUsuario(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Response<LoginResponse>

    // ADIÇÃO 4: Novo método para atualizar o usuário
    // ADICIONE A MESMA ANOTAÇÃO @Headers AQUI
    @Headers("Content-Type: application/json")
    @PUT("v1/gymbuddy/usuario/{email}")
    suspend fun atualizarUsuario(
        @Path("email") email: String,
        @Body request: UsuarioUpdateRequest
    ): Response<Usuario> // A API pode retornar o usuário atualizado
}