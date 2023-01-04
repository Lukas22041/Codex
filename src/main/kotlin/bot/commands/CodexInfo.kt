package bot.commands

import bot.util.BaseCommand
import data.LoadedData
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.message.modify.embed

class CodexInfo : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction) {
        var private = false
        try {
            private = interaction.command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        val response = when(private)
        {
            true -> interaction.deferEphemeralResponse()
            false -> interaction.deferPublicResponse()
            else -> interaction.deferPublicResponse()
        }
        response.respond {

            var list = ""
            for (mod in LoadedData.LoadedModData)
            {
                list += "``${mod.name}``\n"
            }
            embed {
                title = "Info"
                description = "Codex is a bot that can display a variety of data from Starsector and mods. Only mods listed below are included. If you want your mod to be part of the bot, " +
                        "message @Lukas04#0856 on Discord."

                field {
                    name = "Loaded Mods"
                    value = list
                    inline = true
                }

                field {
                    name = "Commands"
                    value = "``/ship <source> <ship id/name>``\n" +
                            "``/weapon <source> <weapon id/name>``\n" +
                            "``/hullmod <source> <hullmod id/name>``\n" +
                            "``/system <source> <system id/name>``\n"
                    inline = true
                }

                footer {
                    text = "Bot by @Lukas04#0856"
                }
            }
        }
    }
}