import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("nome")
    @Expose
    val nome: String,

    @SerializedName("email")
    @Expose
    val email: String,

    @SerializedName("senha")
    @Expose
    val senha: String,

    @SerializedName("nickname")
    @Expose
    val nickname: String
)