package com.thelongevityapp.android.api

import com.thelongevityapp.android.auth.AuthManager
import com.thelongevityapp.android.auth.AuthEvents
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

object RetrofitModule {

    private const val BASE_URL = "https://thelongevityapp-backend-1097215840612.europe-west1.run.app/"

    var authTokenProvider: (() -> String?)? = null
    var languageProvider: (() -> String)? = null

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        authTokenProvider?.invoke()?.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        languageProvider?.invoke()?.let { lang ->
            request.addHeader("X-Language", lang)
        }
        chain.proceed(request.build())
    }

    /** §9.3: 401 → try token refresh → retry; if refresh fails or retry 401 → signOut + notify */
    private val authRetryInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (response.code != 401) return@Interceptor response

        val newToken = runBlocking {
            kotlin.runCatching { AuthManager.getIdToken(forceRefresh = true) }.getOrNull()
        }
        if (newToken == null) {
            AuthManager.signOut()
            AuthEvents.notifyInvalidated()
            return@Interceptor response
        }

        val newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
        val retryResponse = chain.proceed(newRequest)
        if (retryResponse.code == 401) {
            AuthManager.signOut()
            AuthEvents.notifyInvalidated()
        }
        retryResponse
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(authRetryInterceptor)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: LongevityApi get() = retrofit.create(LongevityApi::class.java)
}
