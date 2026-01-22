package com.critetiontech.ctvitalio.UI

import PrefsManager
import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ActivityResetPasswordBinding
import com.critetiontech.ctvitalio.databinding.ActivityUltraHumanBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class UltraHumanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUltraHumanBinding
    private var authService: AuthorizationService? = null
    private lateinit var viewModel: DashboardViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("UH_DEBUG", "UltraHumanActivity CREATED with intent: " + intent.data)
        binding = ActivityUltraHumanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        binding.btnContinue.setOnClickListener {
            initializeAuth()

        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
         binding.btnCancel.setOnClickListener {
            onBackPressed()
        }






    }



    private fun handleAuthRedirectIntent(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "com.critetiontech.ctvitalio" && data.host == "callback") {
            val accessToken = data.getQueryParameter("accessToken")
            val refreshToken = data.getQueryParameter("refreshToken")
            val tokenType = data.getQueryParameter("tokenType")
            val expiry = data.getQueryParameter("expiresIn")

            if (accessToken != null) {
                Log.d("OAuth", "Received code: $accessToken and state: $tokenType")
                Log.d("OAuth", "Received refreshToken: $refreshToken")
                Log.d("OAuth", "Received refreshToken: $tokenType")
                Log.d("OAuth", "Received refreshToken: $expiry")
                viewModel.insertUltraHumanToken(accessToken, refreshToken, tokenType, expiry)
                //Toast.makeText(this, "Check$refreshToken", Toast.LENGTH_LONG).show()
                exchangeCodeForToken(accessToken)
            } else {
                Log.e("OAuth", "No authorization code found in redirect URI")
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("TAG", "onNewIntentCheckingfdbdb1: $intent");
        handleAuthRedirectIntent(intent)
    }

    private fun initializeAuth() {
        authService = AuthorizationService(this)
        startOAuthFlow()
    }

    private fun startOAuthFlow() {
        val authUri = "https://auth.ultrahuman.com/authorise".toUri()
        val tokenUri = "https://partner.ultrahuman.com/oauth/token".toUri()
        val redirectUri = "https://vitalioapi.medvantage.tech:5082/callback".toUri()

        val serviceConfig = AuthorizationServiceConfiguration(authUri, tokenUri)

        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            "W3hWLU2juogFGfgJBdpj3uuaI1n876CwvalFCIFEBKo",
            ResponseTypeValues.CODE,
            redirectUri
        ).setScope("profile ring_data cgm_data ring_extended_data").build()

        val intent = authService!!.getAuthorizationRequestIntent(authRequest)
        startActivity(intent)

    }

    private fun exchangeCodeForToken(authCode: String) {
        val tokenUrl = "https://partner.ultrahuman.com/oauth/token"
        val clientId = "W3hWLU2juogFGfgJBdpj3uuaI1n876CwvalFCIFEBKo"
        val redirectUri = "https://vitalioapi.medvantage.tech:5082/callback"

        val requestBody =
            "grant_type=authorization_code&code=$authCode&redirect_uri=$redirectUri&client_id=$clientId&state=${PrefsManager().getPatient()?.emailID}|1"

        val request = Request.Builder()
            .url(tokenUrl)
            .addHeader("Content-Type", "application/json")
            .post(okhttp3.RequestBody.create(null, requestBody))
            .build()

        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("OAuth", "Token request failed: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    val body = it.body?.string()
                    Log.d("OAuth", "Token response: $body")
                }
            }
        })
    }

}