package com.example.memegenerator1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream


// RetrofitClient setup
object RetrofitClient {
    private const val BASE_URL = "https://meme-api.com/" // Replace with actual URL

    val api: MemeApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MemeApi::class.java)
}

// API interface
interface MemeApi {
    @GET("gimme") // Replace with actual endpoint
    fun getMeme(): Call<MemeResponse>
}

// MemeResponse data class
data class MemeResponse(
    val url: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemeApp()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemeApp() {
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun loadMeme() {
        isLoading = true
        RetrofitClient.api.getMeme().enqueue(object : Callback<MemeResponse> {
            override fun onResponse(call: Call<MemeResponse>, response: Response<MemeResponse>) {
                if (response.isSuccessful &&  response.body() !=null){
                imageUrl = response.body()?.url ?: ""
                Log.d("MemeApp","Loaded meme: $imageUrl") }
                else{
                    Log.e("MemeApp","Response not succesful:${response.errorBody()?.string()}")
                    imageUrl = ""
                }
                isLoading = false
            }

            override fun onFailure(call: Call<MemeResponse>, t: Throwable) {
                Log.e("MemeApp","Network error: ${t.message}",t)
                imageUrl = ""
                isLoading = false
            }
        })
    }

    // Initial meme load
    LaunchedEffect(Unit) {
        loadMeme()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meme Generator") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (imageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Meme",
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Text("Failed to load meme", textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { loadMeme() }) {
                    Text("Next Meme")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val context = LocalContext.current

                    Button(onClick = {
                        saveMeme(context, imageUrl)
                    }) {
                        Text("Save")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(onClick = {
                        shareMeme(context, imageUrl)
                    }) {
                        Text("Share")
                    }
                }

            }
        }
    )
}

fun saveMeme(context: Context, imageUrl: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()

            val result = (loader.execute(request) as SuccessResult).drawable
            val bitmap = (result as BitmapDrawable).bitmap

            val filename = "meme_${System.currentTimeMillis()}.jpg"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Memes")
            }

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Meme saved to Gallery", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save meme", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }
}

fun shareMeme(context: Context, imageUrl: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()

            val result = (loader.execute(request) as SuccessResult).drawable
            val bitmap = (result as BitmapDrawable).bitmap

            val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Meme", null)
            val uri = Uri.parse(path)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/*"
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share meme via"))
        } catch (e: Exception) {
            launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to share meme", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }
}
