package com.barry.currentc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.currentc.common.ext.isValidEmail
import com.barry.currentc.common.ext.isValidPassword
import com.barry.currentc.common.ext.passwordMatches
import com.barry.currentc.common.utility.createBitmapFromUri
import com.barry.currentc.common.utility.normalizeBitmap
import com.barry.currentc.common.utility.saveBitmapToFile
import com.barry.currentc.model.AnnotatedImage
import com.barry.currentc.model.MoneyRecord
import com.barry.currentc.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.Credits
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.ReleaseInfo
import info.movito.themoviedbapi.model.config.TmdbConfiguration
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import com.barry.currentc.R.string as AppText


class MainViewModel : ViewModel() {
    var currentMovieId: Int? = null

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var context: Context
    private var pickImageData: Intent? = null


    suspend fun getPriceChangeToToday(record: MoneyRecord): String =
        withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("YYYY/MM/dd")
            val date = formatter.format(Date())

            return@withContext getPriceChange(
                "united-states",
                "${record.year}/1/1",
                date,
                record.amount
            )
        }

    suspend fun getPriceChange(
        country: String,
        start: String,
        end: String,
        amount: String
    ): String = withContext(Dispatchers.IO) {
        val BASE_URL = "https://www.statbureau.org/calculate-inflation-price-json"
//        Log.d("getPriceChange", "amount: $amount")
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
//                Log.d("getPriceChange", "success: $response")
            }
        } catch (e: Exception) {
            Log.e("getPriceChange", "error :${e.message}")

        }
        return@withContext response
            .toString()
            .replace("\"", "")
            .replace(" ", "")
            .replace("$", "")
    }

    suspend fun getImagesAndAnnotations(movieId: Int?): Map<String, String> {
        if (movieId == null) {
            return emptyMap()
        }

        val imagesRef = storage.reference.child("movies/$movieId")
        val images = try {
            val result = imagesRef.listAll().await()
            result.items.map { fileRef ->
                fileRef.path
            }
        } catch (e: Exception) {
            Log.e("Storage", "Error listing images", e)
            emptyList()
        }
//        Log.d("getImagesAndAnnotations", images.toString())

        val annotationDoc = firestore
            .collection("movies")
            .document(currentMovieId.toString())
            .collection("annotations")
        val annotations: MutableList<AnnotatedImage> = mutableListOf()
        try {
            val documents = annotationDoc.get().await()

            for (doc in documents) {
                annotations.add(doc.toObject<AnnotatedImage>())
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error listing annotations", e)
            emptyMap<String, String>()
        }
//        Log.d("getImagesAndAnnotations", annotations.toString())

        val truncatedFilenames = images.map { it.substringAfterLast("/").substringBeforeLast(".") }

        val resultMap = (truncatedFilenames + annotations.map { it.name })
            .distinct()
            .associateWith { filename ->
                annotations.find { it.name == filename }?.annotation.orEmpty()
            }
        Log.d("getImagesAndAnnotations", resultMap.toString())

        return resultMap
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageLauncher.launch(intent)
    }

    fun setPickImageData(data: Intent?) {
        pickImageData = data
    }

    fun getPickImageData() = pickImageData

    fun handleImageUploadResult(
//        data: Intent?,
        annotation: String?
    ) {
        val data = getPickImageData()
        val time = System.currentTimeMillis()

        if (annotation != null) {
            val annotationDoc = firestore
                .collection("movies")
                .document(currentMovieId.toString())
                .collection("annotations")
                .document(time.toString())

            annotationDoc.set(AnnotatedImage(time.toString(), annotation), SetOptions.merge())
        }

        if (data != null) {
            val fileName = "$time.jpg"
            val imageUri: Uri = data.data!!
            val bitmap = createBitmapFromUri(imageUri, context)
            val largestDimension = if (bitmap.width > bitmap.height) bitmap.width else bitmap.height

            val outputBitmap =
                if (largestDimension >= 100)
                    normalizeBitmap(bitmap, 100)
                else bitmap

            val outputFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                fileName
            )
            saveBitmapToFile(outputBitmap, outputFile)

            val storageRef = storage.reference
            val imageRef = storageRef.child("movies/$currentMovieId/$fileName")

            imageRef.putFile(Uri.fromFile(outputFile))
        }

        setPickImageData(null)
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
                    result.add(doc.toObject<MoneyRecord>())
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

    fun isAnonymousAccount(): Boolean = auth.currentUser?.isAnonymous ?: true


    fun getCurrentUserId(): String = auth.currentUser?.uid.orEmpty()

    fun getCurrentUserEmail(): String = auth.currentUser?.email.orEmpty()

    fun hasUser(): Boolean {
        return auth.currentUser != null
    }

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

    private var email = ""
    private var password = ""
    private var repeatPassword = ""

    fun getEmail(): String {
        return email
    }

    fun setEmail(newEmail: String) {
        email = newEmail
    }

    fun getPassword(): String {
        return password
    }

    fun setPassword(newPassword: String) {
        password = newPassword
    }

    fun getRepeatPassword(): String {
        return repeatPassword
    }

    fun setRepeatPassword(newPassword: String) {
        repeatPassword = newPassword
    }


    suspend fun onSignInClick(
        openAndPopUp: (String, String) -> Unit,
//        settingsScreen: String,
//        loginScreen: String
    ) {
        if (!email.isValidEmail()) {
            Toast
                .makeText(context, AppText.email_error, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password.isBlank()) {
            Toast
                .makeText(context, AppText.empty_password_error, Toast.LENGTH_SHORT)
                .show()
            return
        }

        launchCatching {
            Log.d("onSignInClick", "pre: " + auth.currentUser?.uid)
            authenticate(email, password)
            Log.d("onSignInClick", "post: " + auth.currentUser?.uid)
            openAndPopUp("Settings", "Login")
        }
    }

    fun onForgotPasswordClick() {
        if (!email.isValidEmail()) {
            Toast
                .makeText(context, AppText.email_error, Toast.LENGTH_SHORT)
                .show()
            return
        }

        launchCatching {
            sendRecoveryEmail(email)
            Toast
                .makeText(context, AppText.recovery_email_sent, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        if (!email.isValidEmail()) {
            Toast
                .makeText(context, AppText.email_error, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!password.isValidPassword()) {
            Toast
                .makeText(context, AppText.password_error, Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!password.passwordMatches(repeatPassword)) {
            Toast
                .makeText(context, AppText.password_match_error, Toast.LENGTH_SHORT)
                .show()
            return
        }

        launchCatching {
            linkAccount(email, password)
            openAndPopUp("Settings", "SignUp")
        }
    }

    fun launchCatching(snackbar: Boolean = true, block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Toast
                    .makeText(context, throwable.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
                Log.e("launchCatching", throwable.localizedMessage)
            },
            block = block
        )

    fun setContext(activityContext: Context) {
        context = activityContext
//        account = Auth0(
//            context.getString(R.string.com_auth0_client_id),
//            context.getString(R.string.com_auth0_domain)
//        )
    }
}

