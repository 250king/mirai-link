package com.king250.bot.info

import com.king250.bot.info.provider.Twitter
import kotlin.text.Regex
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.PlainText
import java.net.URL

object Plugin : KotlinPlugin(JvmPluginDescription("com.king250.bot.link", "1.0.0", "链接解析")) {
    override fun onEnable() {
        GlobalEventChannel.subscribeAlways<MessageEvent> {event ->
            try {
                var url: String? = null
                for (i in event.message) {
                    if (i is PlainText) {
                        val rule = Regex("http(s)?://[-a-zA-Z\\d+&@#/%?=~_|!:,.;]*[-a-zA-Z\\d+&@#/%=~_|]")
                        if (rule.matches(i.content)) {
                            url = rule.find(i.content)?.value
                        }
                    }
                }
                if (url != null) {
                    val param = URL(url)
                    when(param.host) {
                        "twitter.com" -> {
                            val provider = Twitter(param)
                            provider.sendMessage(event)
                        }
                    }
                }
            }
            catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
}
