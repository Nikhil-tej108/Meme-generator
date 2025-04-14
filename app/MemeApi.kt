import retrofit2.http.GET
import retrofit2.Call

interface MemeApi {
    @GET("gimme")
    fun getMeme(): Call<MemeResponse>
}
