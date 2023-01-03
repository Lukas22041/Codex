package bot.util

import bot.ButtonData
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildButtonInteraction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import kotlinx.serialization.Serializable




abstract class BaseCommand()
{
    suspend fun init(kord: Kord, commandID: String, commandDesc: String) {
        registerCommand(kord, commandID, commandDesc)

        kord.on<ChatInputCommandInteractionCreateEvent> {if (interaction.command.rootName == commandID) onCommandUse(interaction) }
    }

    open suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {

        }
    }

    abstract suspend fun onCommandUse(interaction: ChatInputCommandInteraction)


}

