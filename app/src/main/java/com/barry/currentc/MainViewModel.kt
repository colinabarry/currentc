package com.barry.currentc

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.barry.currentc.data.User
import info.movito.themoviedbapi.TmdbApi
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.core.MovieResultsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainViewModel : ViewModel() {
    var appJustLaunched by mutableStateOf(true)
    var userIsAuthenticated by mutableStateOf(false)
    var user by mutableStateOf(User())

    private val TAG = "MainViewModel"
    private lateinit var account: Auth0
    private lateinit var context: Context


    suspend fun getMovie(id: Int?): MovieDb? = withContext(Dispatchers.IO) {
        if (id == null) return@withContext null
        val api = TmdbApi(BuildConfig.API_KEY)
        return@withContext api.movies.getMovie(id, "en")
    }

//    suspend fun searchMovies(searchTerm: String, page: Int = 0): MovieResultsPage? =
//        withContext(Dispatchers.IO) {
//            val api = TmdbApi(BuildConfig.API_KEY)
//            return@withContext api.search.searchMovie(searchTerm, 0, "en", false, page)
//        }

    suspend fun searchMovies(searchTerm: String, page: Int = 0): MovieResultsPage? =
        withContext(Dispatchers.IO) {
            val api = TmdbApi(BuildConfig.API_KEY)
            return@withContext api.search.searchMovie(searchTerm, 0, "en-US", false, 0)
        }


    fun login() {
        WebAuthProvider
            .login(account)
            .withScheme(context.getString(R.string.com_auth0_scheme))
            .start(context, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Error occurred in login(): $error")
                }

                override fun onSuccess(result: Credentials) {
                    val idToken = result.idToken

                    // TODO: REMOVE THIS!
                    Log.d(TAG, "ID token: $idToken")

                    user = User(idToken)
                    userIsAuthenticated = true
                    appJustLaunched = false
                }
            })

    }

    fun logout() {
        WebAuthProvider
            .logout(account)
            .withScheme(context.getString(R.string.com_auth0_scheme))
            .start(context, object : Callback<Void?, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Error occurred in logout: $error")
                }

                override fun onSuccess(result: Void?) {
                    user = User()
                    userIsAuthenticated = false
                }
            })
    }

    fun setContext(activityContext: Context) {
        context = activityContext
        account = Auth0(
            context.getString(R.string.com_auth0_client_id),
            context.getString(R.string.com_auth0_domain)
        )
    }
}