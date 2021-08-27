package love.hana.bot.qq.link.bilibili

import org.json.JSONObject

object Bilibili {
    fun video(id: String): JSONObject {
        val url = if (id.startsWith("BV") || id.startsWith("bv")) {
            Kit.sign("https://app.bilibili.com/x/v2/view?bvid=${id}")
        }
        else {
            Kit.sign("https://app.bilibili.com/x/v2/view?aid=${id.subSequence(2, id.length)}")
        }
        val data = JSONObject(Kit.httpGet(url).body?.string())
        if (data.getInt("code") == 0) {
            return data.getJSONObject("data")
        }
        else {
            throw Exception("Something is wrong when we get the data: ${data.getString("message")}")
        }
    }
}