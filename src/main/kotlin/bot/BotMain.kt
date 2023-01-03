package bot

import bot.commands.*
import data.HullmodData
import data.LoadedData
import data.ModData
import data.ShipData
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.File

var token = File("bot-token.txt").readText().trim()

class BotMain
{
    suspend fun init()
    {
        var kord = Kord(token) {

        }

        //Load Commands
        CodexInfo().init(kord, "codex", "Show Codex Bot Information")
        ShowShip().init(kord, "ship", "Displays ship data")
        ShowWeapon().init(kord, "weapon", "Displays weapon data")
        ShowHullmod().init(kord, "hullmod", "Displays weapon data")
        ShowShipsystem().init(kord, "system", "Displays shipsystem data")

        kord.on<GuildButtonInteractionCreateEvent> {
            var response = interaction.deferEphemeralResponse()

            var buttonData: ButtonData = Json.decodeFromString(ButtonData.serializer(), interaction.componentId)

            if (buttonData.buttonID == "delete_post")
            {
                response.respond {
                    if (buttonData.user == interaction.user.memberData.userId.value)
                    {
                        content = "Removed Message"
                        interaction.message.delete("Deleted by user request")
                    }
                    else
                    {
                        content = "Lacking permissions to delete message"
                    }
                }
            }
        }

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
            println("\nBot Started")
        }
    }
}

@Serializable
data class ButtonData(val user: ULong, var buttonID: String)