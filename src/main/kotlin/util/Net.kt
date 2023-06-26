package util


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Net Utils with OKHttp3
 */
object Net {
    fun <T> get(
        url: String,
        header: Map<String, String> = emptyMap(),
        query: Map<String, Any> = emptyMap(),
        use: (Response) -> T,
    ): Deferred<T?> {
        val builder = Request.Builder()
            .method("GET", null)
            .url("$url?" + query.entries.joinToString("&") { "${it.key}=${it.value}" })
        for ((key, value) in header) {
            builder.addHeader(key, value)
        }
        val client = OkHttpClient()
        val request = builder.build()

        return CoroutineScope(Dispatchers.IO).async {
            client.newCall(request).execute().use(use)
        }
    }
}
