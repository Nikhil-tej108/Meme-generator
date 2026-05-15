package com.example.memegenerator1

import kotlinx.coroutines.withContext
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException

// RetrofitClient setup
object RetrofitClient {
    private const val BASE_URL = "https://meme-api.com/"

    val api: MemeApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MemeApi::class.java)
}

interface MemeApi {
    @GET("gimme/wholesomememes")
    fun getMeme(): Call<MemeResponse>
}

data class MemeResponse(
    val url: String,
    val title: String? = null,
    val author: String? = null,
    val subreddit: String? = null
) {
    val shareText: String
        get() = """
            Check out this meme from r/$subreddit!
            "$title" by u/$author
        """.trimIndent()
}

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
    var isSaving by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun loadMeme() {
        isLoading = true
        RetrofitClient.api.getMeme().enqueue(object : Callback<MemeResponse> {
            override fun onResponse(call: Call<MemeResponse>, response: Response<MemeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    imageUrl = response.body()?.url ?: ""
                    Log.d("MemeApp", "Loaded meme: $imageUrl")
                } else {
                    Log.e("MemeApp", "Response not successful: ${response.errorBody()?.string()}")
                    imageUrl = ""
                }
                isLoading = false
            }

            override fun onFailure(call: Call<MemeResponse>, t: Throwable) {
                Log.e("MemeApp", "Network error: ${t.message}", t)
                imageUrl = ""
                isLoading = false
            }
        })
    }

    LaunchedEffect(Unit) {
        loadMeme()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meme Generator") }) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00FF00), // Neon Green
                                Color(0xFF0000FF)  // Neon Blue
                            )
                        )
                    ),
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
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Failed to load meme", textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadMeme() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { loadMeme() },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading...")
                        }
                    } else {
                        Text("Next Meme")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            isSaving = true
                            CoroutineScope(Dispatchers.IO).launch {
                                saveMeme(context, imageUrl)
                                isSaving = false
                            }
                        },
                        enabled = !isSaving && imageUrl.isNotEmpty()
                    ) {
                        if (isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            }
                        } else {
                            Text("Save")
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            isSharing = true
                            CoroutineScope(Dispatchers.IO).launch {
                                shareMeme(context, imageUrl)
                                isSharing = false
                            }
                        },
                        enabled = !isSharing && imageUrl.isNotEmpty()
                    ) {
                        if (isSharing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sharing...")
                            }
                        } else {
                            Text("Share")
                        }
                    }
                }
            }
        }
    )
}
suspend fun saveMeme(context: Context, imageUrl: String) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Memes")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                    throw IOException("Failed to save bitmap")
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Meme saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        } ?: throw IOException("Failed to create media entry")
    } catch (e: Exception) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Failed to save meme: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        Log.e("MemeApp", "Save error", e)
    }
}

suspend fun shareMeme(context: Context, imageUrl: String) {
    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        val result = (loader.execute(request) as SuccessResult).drawable
        val bitmap = (result as BitmapDrawable).bitmap

        val filename = "meme_share_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Memes")
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: throw IOException("Failed to create share file")

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                throw IOException("Failed to compress bitmap")
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        CoroutineScope(Dispatchers.Main).launch{
            context.startActivity(Intent.createChooser(shareIntent, "Share meme via"))
        }
    } catch (e: Exception) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Failed to share meme", Toast.LENGTH_SHORT).show()
        }
        Log.e("MemeApp", "Share error", e)
    }
}
