package love.hana.bot.qq.link.bilibili

import com.mongodb.client.model.Filters
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.bson.Document
import java.io.InputStream
import java.math.BigInteger
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.Instant

object Kit {
    private val DB_CONNECT = MongoClients.create("mongodb://root:55107888AAA@127.0.0.1")

    val CONFIG: MongoCollection<Document> = DB_CONNECT.getDatabase("qq-bot").getCollection("link-bilibili")

    private val KMS = DB_CONNECT.getDatabase("website").getCollection("kms")

    private val KEY = KMS.find(Filters.eq("platform", "bilibili.api")).first()

    private val ACCESS_TOKEN = KEY?.getString("access_token")

    private val CLIENT_ID = KEY?.getString("client_id")

    private val CLIENT_SECRET = KEY?.getString("client_secret")

    fun sign(url: String): String {
        val timestamp = Instant.now().epochSecond
        val completeUrl = "${url}&access_key=${ACCESS_TOKEN}&appkey=${CLIENT_ID}&build=6042000&device=android&mobi_app=android&platform=android&ts=${timestamp}"
        val param = URI(completeUrl)
        val set = HashMap<String, String>()
        for (i in param.query.split("&")) {
            val child = i.split("=")
            set[child[0]] = child[1]
        }
        val keys = ArrayList<String>(set.keys)
        keys.sort()
        var sort = ""
        var first = true
        for (i in keys) {
            if (first) {
                first = false
            }
            else {
                sort += "&"
            }
            sort += "${i}=${set[i]}"
        }
        val encrypt = MessageDigest.getInstance("md5").digest("${sort}${CLIENT_SECRET}".toByteArray())
        val signature = BigInteger(1, encrypt).toString(16)
        return "${completeUrl}&sign=${signature}"
    }

    fun isoTime(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(timestamp)
    }

    fun realLink(url: String): String {
        val client = OkHttpClient.Builder().followRedirects(false).build()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val origin = response.header("Location").toString()
        response.close()
        return origin
    }

    fun httpGet(url: String): Response {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute()
    }

    fun download(url: String): InputStream? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body?.byteStream()
    }
}