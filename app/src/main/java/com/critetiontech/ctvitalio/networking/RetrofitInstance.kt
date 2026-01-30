import com.critetiontech.ctvitalio.networking.ApiService
import com.critetiontech.ctvitalio.networking.RetrofitFactory

object RetrofitInstance {

    const val DEFAULT_BASE_URL = "http://182.156.200.177:5082/"
    const val DEFAULT_BASE_URL_7096 = "http://182.156.200.177:5096/"
    const val DEFAULT_BASE_URL_7083 = "http://182.156.200.177:5083/"
    const val DEFAULT_BASE_URL_7082 = "http://182.156.200.177:5082/"
    const val DEFAULT_BASE_URL_5119 = "http://182.156.200.177:5119/"
    const val DEFAULT_BASE_URL_5100 = "http://182.156.200.177:5100/"  //ok
    const val DEFAULT_BASE_URL_5090 = "http://182.156.200.177:5090/"
    const val SHOPRIGHT = "http://food.shopright.ai:3478/api/"

    val uploadLabreportUrl = "http://182.156.200.178:8016/uploadLabreport/"
    val holdSpeakWsUrl ="ws://182.156.200.177:8002/listen?token="
    // -------- GENERIC ----------
    fun createApiService(
        overrideBaseUrl: String? = null
    ): ApiService {
        return apiFor(overrideBaseUrl ?: DEFAULT_BASE_URL)
    }

    // -------- SPECIFIC ----------
    fun createApiService5100(): ApiService =
        apiFor(DEFAULT_BASE_URL_5100)

    fun createApiService7096(): ApiService =
        apiFor(DEFAULT_BASE_URL_7096)

    fun createApiService7083(): ApiService =
        apiFor(DEFAULT_BASE_URL_7083)

    fun createApiService7082(): ApiService =
        apiFor(DEFAULT_BASE_URL_7082)

    fun createApiService5119(): ApiService =
        apiFor(DEFAULT_BASE_URL_5119)

    fun createApiService5090(): ApiService =
        apiFor(DEFAULT_BASE_URL_5090)

    fun createShopRightApi(): ApiService =
        apiFor(SHOPRIGHT)

    // -------- INTERNAL ----------
    private fun apiFor(baseUrl: String): ApiService {
        return RetrofitFactory
            .getRetrofit(baseUrl)
            .create(ApiService::class.java)
    }
}
