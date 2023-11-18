package bot.commands

import bot.util.BaseCommand
import bot.util.CommandUtil
import bot.util.CommandUtil.abbreviate
import data.LoadedData
import data.StarmodderData
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.embed

class StarmodderCommand : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("name", "Name of the mod. Should be as precise as possible") { required = true}
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction) {
        var private = false
        val command = interaction.command
        val input = command.strings["name"]!!

        try {
            private = interaction.command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        var data = CommandUtil.getFuzzyStarmodder(input)
        if (data == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Unable to find mod going by $input" }
            return
        }

        val response = when(private)
        {
            true -> interaction.deferEphemeralResponse()
            false -> interaction.deferPublicResponse()
            else -> interaction.deferPublicResponse()
        }
        response.respond {


            embed {
                var titleString = "Mod: ${data.name}"
                if (data.gameVersion != null) titleString += " (${data.gameVersion})"

                title = titleString
                var summary = data.summary ?: "No Summary available"
                description = "$summary"

                if (data.imageURL != null) {
                    image = data.imageURL
                }

                var links: String = ""
                if (data.downloadLink != null) links += "``Download:`` ${data.downloadLink}\n"
                if (data.forumLink != null) links += "``Forum:`` ${data.forumLink}\n"
                if (data.discordLink != null) links += "``Discord:`` <${data.discordLink}>"
                if (links != "") {
                    field {
                        name = "Links"
                        value = links
                    }
                }

                if (data.author != null) {
                    footer {
                        text = "Author: ${data.author}"
                    }
                }
            }
        }
    }
}