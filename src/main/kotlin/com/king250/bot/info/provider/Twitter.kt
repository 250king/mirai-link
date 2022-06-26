package com.king250.bot.info.provider

import com.alibaba.fastjson.JSON
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL

class Twitter constructor(url: URL) {
    private val client = OkHttpClient()

    private var token = ""

    private var id = ""

    init {
        val param = url.path.split("/")
        id = param[param.size - 1]
        val request = Request.Builder()
            .url("https://api.twitter.com/1.1/guest/activate.json")
            .header("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA")
            .post("".toRequestBody(null))
            .build()
        val response = client.newCall(request).execute()
        val data = JSON.parseObject(response.body?.string())
        response.close()
        token = data.getString("guest_token")
    }

    suspend fun sendMessage(target: MessageEvent) {
        var request = Request.Builder()
            .url("https://twitter.com/i/api/graphql/epE7u5YkRlvyGQ4DCTlAtA/TweetDetail?variables=%7B%22focalTweetId%22%3A%22${id}%22%2C%22with_rux_injections%22%3Afalse%2C%22includePromotedContent%22%3Atrue%2C%22withCommunity%22%3Atrue%2C%22withQuickPromoteEligibilityTweetFields%22%3Atrue%2C%22withBirdwatchNotes%22%3Afalse%2C%22withSuperFollowsUserFields%22%3Atrue%2C%22withDownvotePerspective%22%3Afalse%2C%22withReactionsMetadata%22%3Afalse%2C%22withReactionsPerspective%22%3Afalse%2C%22withSuperFollowsTweetFields%22%3Atrue%2C%22withVoice%22%3Atrue%2C%22withV2Timeline%22%3Atrue%7D&features=%7B%22dont_mention_me_view_api_enabled%22%3Atrue%2C%22interactive_text_enabled%22%3Afalse%2C%22responsive_web_uc_gql_enabled%22%3Afalse%2C%22vibe_tweet_context_enabled%22%3Afalse%2C%22responsive_web_edit_tweet_api_enabled%22%3Afalse%2C%22standardized_nudges_misinfo%22%3Afalse%2C%22responsive_web_enhance_cards_enabled%22%3Afalse%7D")
            .header("Authorization", "Bearer AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA")
            .header("x-guest-token", token)
            .build()
        var response = client.newCall(request).execute()
        val data = JSON.parseObject(response.body?.string())
        val source = data.getJSONObject("data").getJSONObject("threaded_conversation_with_injections_v2").getJSONArray("instructions").getJSONObject(0).getJSONArray("entries").getJSONObject(0).getJSONObject("content").getJSONObject("itemContent").getJSONObject("tweet_results").getJSONObject("result")
        val user = data.getJSONObject("core").getJSONObject("user_results").getJSONObject("result").getJSONObject("legacy")
        val tweet = source.getJSONObject("legacy")
        val message = buildMessageChain {
            add(QuoteReply(target.source))
            add(PlainText(user.getString("name")))
            add(PlainText(tweet.getString("full_text")))
            if (tweet.containsKey("extended_entities") && tweet.getJSONObject("extended_entities").containsKey("media")) {
                val list = tweet.getJSONObject("extended_entities").getJSONArray("media")
                for (i in 0 until list.size) {
                    request = Request.Builder()
                        .url(list.getString(i))
                        .build()
                    response = client.newCall(request).execute()
                    val resource = response.body?.byteStream()?.toExternalResource()
                    val image = resource?.let {
                        target.subject.uploadImage(it)
                    }
                    image?.let {
                        add(Image(image.imageId))
                    }
                }
            }
        }
        target.subject.sendMessage(message)
    }
}