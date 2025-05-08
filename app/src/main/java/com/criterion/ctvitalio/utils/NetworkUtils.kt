import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.critetiontech.ctvitalio.utils.MyApplication

object NetworkUtils {

    // Checks for internet connectivity
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Checks and shows toast if no internet
    fun checkAndShowToast(): Boolean {
        val context = MyApplication.appContext
        val isConnected = isInternetAvailable(MyApplication.appContext)

        if (!isConnected) {
            Toast.makeText(context, "📶 No internet connection", Toast.LENGTH_SHORT).show()
        }

        return isConnected
    }

    fun getConnectionQuality(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No Connection"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No Connection"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Strong (Wi-Fi)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.linkDownstreamBandwidthKbps >= 5000 -> "Strong (4G/5G)"
                    capabilities.linkDownstreamBandwidthKbps >= 1000 -> "Moderate (3G/4G)"
                    capabilities.linkDownstreamBandwidthKbps >= 100 -> "Weak (2G/3G)"
                    else -> "Very Weak"
                }
            }
            else -> "Unknown"
        }
    }

    fun getDownlinkSpeedMbps(context: Context): Double {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return 0.0
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return 0.0

        val kbps = capabilities.linkDownstreamBandwidthKbps
        return kbps / 1000.0 // Mbps
    }



    object ThemeHelper {
        const val MODE_LIGHT = "light"
        const val MODE_DARK = "dark"
        const val MODE_SYSTEM = "system"

        fun applyTheme(themePref: String) {
            when (themePref) {
                MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }



}
