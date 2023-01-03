package bot.commands

import bot.util.BaseCommand
import data.LoadedData
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.modify.embed

class CodexInfo : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {

        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction) {
        val response = interaction.deferPublicResponse()
        response.respond {

            var list = ""
            for (mod in LoadedData.LoadedModData)
            {
                list += "``${mod.name} (ID: ${mod.id})``\n"
            }
            embed {
                title = "Info"
                description = "Codex is a bot that can display a variety of data from Starsector and mods. Only mods listed below are included. If you want your mod to be part of the bot, " +
                        "message @Lukas04#0856 on Discord."

                field {
                    name = "Loaded Mods"
                    value = list
                }

                footer {
                    text = "Bot by @Lukas04#0856"
                }
            }
        }
    }
}