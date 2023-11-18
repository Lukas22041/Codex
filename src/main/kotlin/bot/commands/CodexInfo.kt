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

            var list = "``"
            for (mod in LoadedData.LoadedModData)
            {
                list += "${mod.name}, "
            }
            var lastComma = list.lastIndexOf(",")
            list = list.substring(0, lastComma)
            list = "$list``"
            embed {
                title = "Info"
                description = "Codex is a bot that shows information about Starsector and some mod related data. "

                field {
                    name = "Starsector Commands"
                    value = "``/ship <source> <ship id/name>``\n" +
                            "``/weapon <source> <weapon id/name>``\n" +
                            "``/hullmod <source> <hullmod id/name>``\n" +
                            "``/system <source> <system id/name>``\n"
                    inline = false
                }

                field {
                    name = "Starmodder Commands"
                    value = "Used to look through **starmodder.pages.dev** to find mod related data.\n" +
                            "``/mod <name> <ship id/name>``\n"
                    inline = false
                }

                /*field {
                    name = "Adding Mods & Support"
                    value = "Currently not supporting mods, this may change again a while after mods have updated to 0.96."
                }

                field {
                    name = "Loaded Content"
                    value = list
                    inline = false
                }*/

                footer {
                    text = "Bot by @Lukas04#0856"
                }
            }
        }
    }
}