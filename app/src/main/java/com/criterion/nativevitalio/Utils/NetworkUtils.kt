import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.criterion.nativevitalio.Utils.MyApplication

object NetworkUtils {

    // Checks for internet connectivity
    private fun isInternetAvailable(): Boolean {
        val context = MyApplication.appContext
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    // Checks and shows toast if no internet
    fun checkAndShowToast(): Boolean {
        val context = MyApplication.appContext
        val isConnected = isInternetAvailable()

        if (!isConnected) {
            Toast.makeText(context, "📶 No internet connection", Toast.LENGTH_SHORT).show()
        }

        return isConnected
    }
}
