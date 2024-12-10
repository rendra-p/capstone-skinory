package com.capstone.skinory.ui.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.capstone.skinory.data.UserPreferences
import com.capstone.skinory.data.remote.retrofit.ApiConfig
import com.capstone.skinory.data.remote.retrofit.ApiService
import com.capstone.skinory.ui.MainActivity
import com.capstone.skinory.databinding.ActivitySplashBinding
import com.capstone.skinory.ui.analysis.AnalysisActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@Suppress("DEPRECATION")
class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var tokenDataStore: TokenDataStore
    private lateinit var userPreferences: UserPreferences
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        tokenDataStore = TokenDataStore.getInstance(this)
        userPreferences = UserPreferences(this)
        apiService = ApiConfig.getApiService(tokenDataStore)

        Handler().postDelayed({
            checkToken()
        }, 2000)
    }

    private fun checkToken() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!isInternetAvailable()) {
                    withContext(Dispatchers.Main) {
                        navigateToErrorActivity("no_internet")
                    }
                    return@launch
                }

                tokenDataStore.token.first { token ->
                    if (!token.isNullOrEmpty()) {
                        val userId = userPreferences.getUserId()

                        if (userId != null) {
                            val result = validateToken(userId, token)

                            withContext(Dispatchers.Main) {
                                if (result) {
                                    navigateToAnalysisActivity()
                                } else {
                                    navigateToLoginActivity()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                navigateToLoginActivity()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            navigateToLoginActivity()
                        }
                    }
                    true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    navigateToErrorActivity("unknown", e.message)
                }
            }
        }
    }

    private suspend fun validateToken(userId: String, token: String): Boolean {
        return try {
            val formattedToken = "Bearer $token"

            apiService.getProfile(userId, formattedToken)

            true
        } catch (e: HttpException) {
            when (e.code()) {
                403, 401 -> {
                    tokenDataStore.clearToken()
                    false
                }
                else -> {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun navigateToAnalysisActivity() {
        startActivity(Intent(this, MainActivity::class.java))
//        startActivity(Intent(this, AnalysisActivity::class.java))
        finish()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToErrorActivity(
        errorType: String,
        errorMessage: String? = null
    ) {
        startActivity(Intent(this, ErrorActivity::class.java).apply {
            putExtra("error_type", errorType)
            errorMessage?.let {
                putExtra("error_message", it)
            }
        })
        finish()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}