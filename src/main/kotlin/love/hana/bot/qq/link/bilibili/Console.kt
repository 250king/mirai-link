package love.hana.bot.qq.link.bilibili

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender

object Console: CompositeCommand(Plugin, "link-bilibili") {
    @SubCommand
    suspend fun ConsoleCommandSender.enable(groupID: Long) {
        Plugin.enable(groupID)
    }

    @SubCommand
    suspend fun ConsoleCommandSender.disable(groupID: Long) {
        Plugin.disable(groupID)
    }
}
