package com.barry.currentc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import com.barry.currentc.common.utility.createBitmapFromUri
import com.barry.currentc.common.utility.normalizeBitmap
import com.barry.currentc.common.utility.saveBitmapToFile
import com.barry.currentc.model.MoneyRecord
import com.barry.currentc.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.Credits
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.ReleaseInfo
import info.movito.themoviedbapi.model.config.TmdbConfiguration
import info.movito.themoviedbapi.model.core.MovieResultsPage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date


class MainViewModel : ViewModel() {
    var currentMovieId: Int? = 769

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var context: Context

    private val httpClient = HttpClient(OkHttp)


    suspend fun getPriceChangeToToday(record: MoneyRecord): String =
        withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("YYYY/MM/dd")
            val date = formatter.format(Date())

            return@withContext getPriceChange(
                "united-states",
                "${record.year}/1/1",
                date,
                record.amount.toFloat()
            )
        }

    suspend fun getPriceChange(
        country: String,
        start: String,
        end: String,
        amount: Float
    ): String = withContext(Dispatchers.IO) {
        val BASE_URL = "https://www.statbureau.org/calculate-inflation-price-json"
        val url = URL("$BASE_URL?country=$country&start=$start&end=$end&amount=$amount")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.doInput = true

        val response = StringBuilder()
        try {
            val reader = InputStreamReader(connection.inputStream)
            reader.use { input ->
                val bufferedReader = BufferedReader(input)

                bufferedReader.forEachLine {
                    response.append(it.trim())
                }
                Log.d("getPriceChange", "success: $response")
            }
        } catch (e: Exception) {
            Log.d("getPriceChange", "error :${e.message}")

        }
        return@withContext response.toString().replace('"', ' ').trim()
    }

    suspend fun getImages(movieId: Int?): List<String> {
        if (movieId == null) {
            return emptyList()
        }

        val storageRef = storage.reference.child("movies/$movieId")

        return try {
            val result = storageRef.listAll().await()
            result.items.map { fileRef ->
                fileRef.path
            }
        } catch (e: Exception) {
            Log.e("Storage", "Error listing images", e)
            emptyList()
        }
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageLauncher.launch(intent)
    }

    fun handleImageUploadResult(data: Intent?) {
        if (data == null) {
            // No image selected
            return
        }

        val time = System.currentTimeMillis()
        val fileName = "$time.jpg"

        val imageUri: Uri = data.data!!
        val bitmap = createBitmapFromUri(imageUri, context)
        val largestDimension = if (bitmap.width > bitmap.height) bitmap.width else bitmap.height

        val outputBitmap =
            if (largestDimension >= 1000)
                normalizeBitmap(bitmap, 1000)
            else bitmap

        val outputFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            fileName
        )
        saveBitmapToFile(outputBitmap, outputFile)

        // Get a reference to the Firebase Storage
        val storageRef = storage.reference
        // Create a reference to the image location in Firebase Storage
        val imageRef = storageRef.child("movies/$currentMovieId/$fileName")

        // Upload the image to Firebase Storage
        imageRef.putFile(Uri.fromFile(outputFile))
            .addOnProgressListener {// TODO: Implement this
                var totalBytes = it.totalByteCount
                var bytesTransferred = it.bytesTransferred
            }
            .addOnSuccessListener {
                // Image uploaded successfully
                // Handle success, e.g., show a success message or update UI
            }
            .addOnFailureListener {
                // Handle failure
                // Log error or show an error message to the user
            }
    }

    suspend fun getMoneyRecords(movieId: Int?): List<MoneyRecord> =
        withContext(Dispatchers.IO) {
            if (movieId == null) return@withContext listOf()

            val moneyRecordsCollection = firestore
                .collection("movies")
                .document(movieId.toString())
                .collection("moneyRecords")

            val result: MutableList<MoneyRecord> = mutableListOf()
            try {
                val documents = moneyRecordsCollection
                    .get().await()

                for (doc in documents) {
                    result.add(doc.toObject(MoneyRecord::class.java))
                }
            } catch (e: Exception) {
                Log.e("getMoneyRecords", e.localizedMessage)
            }

            return@withContext result.sortedBy { it.year }
        }

    suspend fun addMoneyRecord(movieId: Int?, record: MoneyRecord): Boolean =
        withContext(Dispatchers.IO) {
            if (movieId == null) return@withContext false

            val time = System.currentTimeMillis()
            val movieDoc = firestore
                .collection("movies")
                .document(movieId.toString())
                .collection("moneyRecords")
                .document(time.toString())

            val success = try {
                movieDoc.set(record, SetOptions.merge()).await()
                true
            } catch (e: Exception) {
                false
            }

            return@withContext success
        }
//suspend fun addMoneyRecord(movieId: Int?, amount: Float): Boolean =
//        withContext(Dispatchers.IO) {
//            if (movieId == null) return@withContext false
//
//            try {
//                val movieRef = firestore.collection("movies").document(movieId.toString())
//
//                // Check if document exists
//                val movieDocSnapshot = movieRef.get().await()
//                // Create document if necessary
//                if (!movieDocSnapshot.exists()) {
//                    movieRef.set(mapOf("moneyRecords" to mutableListOf<Float>())).await()
//                }
//
//                // Retrieve the updated document snapshot
//                val updatedSnapshot = movieRef.get().await()
//
//                // Update the moneyRecords field
//                val moneyRecords = updatedSnapshot
//                    .data
//                    ?.get("moneyRecords") as? MutableList<Float> ?: mutableListOf()
//                moneyRecords.add(amount)
//                movieRef.update("moneyRecords", moneyRecords).await()
//            } catch (e: Exception) {
//                Log.e("Firestore", "Error adding money record", e)
//                return@withContext false
//            }
//            return@withContext true
//        }


    suspend fun getMovie(id: Int?): MovieDb? =
        withContext(Dispatchers.IO) {
            if (id == null) return@withContext null

            currentMovieId = id
            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.movies.getMovie(id, "en")
        }

    suspend fun getCredits(id: Int?): Credits? =
        withContext(Dispatchers.IO) {
            if (id == null) return@withContext null

            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.movies.getCredits(id)
        }

    suspend fun getReleaseInfo(id: Int?): List<ReleaseInfo>? =
        withContext(Dispatchers.IO) {
            if (id == null) return@withContext null

            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.movies.getReleaseInfo(id, "en-US")
        }

    suspend fun getApiConfig(): TmdbConfiguration =
        withContext(Dispatchers.IO) {
            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.configuration
        }

    suspend fun searchMovies(searchTerm: String, page: Int = 0): MovieResultsPage? =
        withContext(Dispatchers.IO) {
            if (searchTerm.isEmpty()) return@withContext null

            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.search.searchMovie(searchTerm, 0, "en-US", false, page)
        }

    suspend fun getPopularMovies(): MovieResultsPage? =
        withContext(Dispatchers.IO) {
            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.movies.getPopularMovies("en-US", 0)
        }

    suspend fun getTopRatedMovies(): MovieResultsPage? =
        withContext(Dispatchers.IO) {
            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.movies.getTopRatedMovies("en-US", 0)
        }

    suspend fun getNowPlayingMovies(): MovieResultsPage? =
        withContext(Dispatchers.IO) {
            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.movies.getNowPlayingMovies("en-US", 0, "US")
        }

    val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    val hasUser: Boolean
        get() = auth.currentUser != null

    val currentUser: Flow<User>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid, it.isAnonymous) } ?: User())
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun authenticate(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun sendRecoveryEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun createAnonymousAccount() {
        auth.signInAnonymously().await()
    }

    suspend fun linkAccount(email: String, password: String): Unit {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential).await()
    }

    suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }

    suspend fun signOut() {
        if (auth.currentUser!!.isAnonymous) {
            auth.currentUser!!.delete()
        }
        auth.signOut()

        // Sign the user back in anonymously.
        createAnonymousAccount()
    }

    fun setContext(activityContext: Context) {
        context = activityContext
//        account = Auth0(
//            context.getString(R.string.com_auth0_client_id),
//            context.getString(R.string.com_auth0_domain)
//        )
    }
}