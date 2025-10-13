package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R

class Ring_Connectivity : Fragment() {

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Handle redirect intent if present
    handleAuthRedirectIntent(intent)

    initializeAuth()
}


override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleAuthRedirectIntent(intent)
}
private fun handleAuthRedirectIntent(intent: Intent) {
    val data = intent.data
    if (data != null && data.scheme == "com.example.oauth" && data.host == "callback") {
        val code = data.getQueryParameter("accessToken")
        val refreshToken = data.getQueryParameter("refreshToken")
        val state = data.getQueryParameter("state")

        if (code != null) {
            Log.d("OAuth", "Received code: $code and state: $state")
            Log.d("OAuth", "Received refreshToken: $code")
            exchangeCodeForToken(code)
        } else {
            Log.e("OAuth", "No authorization code found in redirect URI")
        }
    }
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
    ).setScope("profile ring_data cgm_data").build()

    val intent = authService!!.getAuthorizationRequestIntent(authRequest)
    startActivity(intent)
}

private fun exchangeCodeForToken(authCode: String) {
    val tokenUrl = "https://partner.ultrahuman.com/oauth/token"
    val clientId = "W3hWLU2juogFGfgJBdpj3uuaI1n876CwvalFCIFEBKo"
    val redirectUri = "https://vitalioapi.medvantage.tech:5082/callback"

    val requestBody =
        "grant_type=authorization_code&code=$authCode&redirect_uri=$redirectUri&client_id=$clientId&state=animesh.singh0108@gmail.com"

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
                val body = it.body.string()
                Log.d("OAuth", "Token response: $body")
            }
        }
    })
}


override fun onDestroy() {
    super.onDestroy()
    authService?.dispose()
}

}
}
