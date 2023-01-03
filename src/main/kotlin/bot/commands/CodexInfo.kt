package bot.commands

import bot.util.BaseCommand
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ChatInputCommandInteraction

class CodexInfo : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {

        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction) {

    }




}