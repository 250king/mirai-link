package love.hana.bot.qq.link.bilibili

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URI
import java.io.IOException

object Plugin: KotlinPlugin(JvmPluginDescription("love.hana.bot.qq.link", "2.0.0", "链接解析")) {
    private val CONFIG = ArrayList<Long>()

    fun enable(groupID: Long) {
        if (Kit.CONFIG.find(Filters.eq("groupID", groupID)).first() == null) {
            logger.error("The Group $groupID isn't existed!")
        }
        else {
            if (CONFIG.contains(groupID)) {
                logger.warning("The Group $groupID has been enabled!")
            }
            else {
                CONFIG.add(groupID)
                val new = Updates.combine(
                    Updates.set("enable", true)
                )
                val setting = UpdateOptions().upsert(true)
                Kit.CONFIG.updateOne(Filters.eq("groupID", groupID), new, setting)
            }
        }
    }

    fun disable(groupID: Long) {
        if (CONFIG.contains(groupID)) {
            CONFIG.remove(groupID)
            val new = Updates.combine(
                Updates.set("enable", false)
            )
            val setting = UpdateOptions().upsert(true)
            Kit.CONFIG.updateOne(Filters.eq("groupID", groupID), new, setting)
        }
        else {
            logger.warning("The Group $groupID has been disabled!")
        }
    }

    override fun onEnable() {
        CommandManager.registerCommand(Console)
        val groups = Kit.CONFIG.find()
        for (i in groups) {
            if (i.getBoolean("enable")) {
                CONFIG.add(i.getLong("groupID"))
            }
        }
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {event ->
            if (CONFIG.contains(event.group.id)) {
                try {
                    for (i in event.message) {
                        var url: String? = null
                        if (i is PlainText) {
                            url = Regex("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?").find(i.content)?.value
                        }
                        else if (i is ServiceMessage) {
                            val data = Jsoup.parse(i.content)
                            val tag = data.getElementsByTag("msg")[0]
                            if (tag.attr("action").equals("web")) {
                                url = tag.attr("url")
                            }
                        }
                        else if (i is LightApp) {
                            val data = JSONObject(i.content)
                            if (data.getString("app").equals("com.tencent.structmsg")) {
                                url = data.getJSONObject("meta").getJSONObject("news").getString("jumpUrl")
                            }
                            else if (data.getString("app").equals("com.tencent.miniapp_01")) {
                                url = data.getJSONObject("meta").getJSONObject("detail_1").getString("qqdocurl")
                            }
                        }
                        if (url != null) {
                            var param = URI(url)
                            if (param.host.equals("b23.tv")) {
                                url = Kit.realLink(url)
                                param = URI(url)
                            }
                            if (param.host.equals("bilibili.com") || param.host.equals("www.bilibili.com")) {
                                if (param.path.startsWith("/video/")) {
                                    var id = param.path.split("/")[2]
                                    val data = Bilibili.video(id)
                                    val stream = Kit.download(data.getString("pic"))?.toExternalResource()
                                    if (stream != null) {
                                        val image = event.group.uploadImage(stream)
                                        id = image.imageId
                                        val achieve = data.getJSONObject("stat")
                                        val message = buildMessageChain {
                                            add(QuoteReply(event.source))
                                            add(Image.fromId(id))
                                            add(PlainText("${data.getString("title")}\n\n"))
                                            add(PlainText("${data.getString("desc")}\n\n"))
                                            add(PlainText("UP主：${data.getJSONObject("owner").getString("name")}\n"))
                                            add(PlainText("上传时间：${Kit.isoTime(data.getLong("pubdate") * 1000L)}\n"))
                                            add(PlainText("弹幕量：${achieve.getLong("danmaku")}\n"))
                                            add(PlainText("观看量：${achieve.getLong("view")}\n"))
                                            add(PlainText("回复量：${achieve.getLong("reply")}\n"))
                                            add(PlainText("硬币量：${achieve.getLong("coin")}\n"))
                                            add(PlainText("分享量：${achieve.getLong("share")}\n"))
                                            add(PlainText("点赞量：${achieve.getLong("like")}\n"))
                                            add(PlainText("收藏量：${achieve.getInt("favorite")}"))
                                        }
                                        event.group.sendMessage(message)
                                    }
                                    else {
                                        throw IOException("We can't get the image from ${data.getString("pic")}")
                                    }
                                }
                            }
                        }
                    }
                }
                catch (exception: Exception){
                    exception.printStackTrace()
                }
            }
        }
    }
}