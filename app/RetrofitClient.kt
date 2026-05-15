import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Request

object RetrofitClient {
    private const val BASE_URL = "https://meme-api.com/"

    val api: MemeApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MemeApi::class.java)
}

