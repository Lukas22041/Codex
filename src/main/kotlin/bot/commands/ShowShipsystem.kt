package bot.commands

import bot.ButtonData
import bot.util.BaseCommand
import bot.util.CommandUtil
import bot.util.CommandUtil.getFuzzyMod
import bot.util.CommandUtil.getFuzzyShipsystem
import bot.util.CommandUtil.trimAfter
import data.LoadedData
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.serialization.json.Json

class ShowShipsystem : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true}
            string("system", "name or id of the weapon") { required = true}
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        //Look for Mod and Weapon data
        val command = interaction.command
        val modInput = command.strings["source"]!!
        val systemInput = command.strings["system"]!!
        var private = false
        try {
            private = command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        var modData = CommandUtil.loadModData(modInput, interaction) ?: return
        if (LoadedData.LoadedShipsystemData.get(modData.id).isNullOrEmpty())
        {
            interaction.deferEphemeralResponse().respond { content = "Requested mod \"${modData.name}\" has no shipsystems." }
            return
        }
        var systemData = LoadedData.LoadedShipsystemData.get(modData.id)!!.find { it.id.lowercase() == systemInput.lowercase() || it.name.lowercase() == systemInput.lowercase() }
        if (systemData == null) systemData = getFuzzyShipsystem(modData.id, systemInput)
        if (systemData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Unable to find shipsystem going by \"$systemInput\" in ${modData.name}" }
            return
        }

        //Setup general data required for the card
        var systemDescriptionsData = LoadedData.LoadedDescriptionData.get(modData.id)!!.find { it.id == systemData.id }

        var canBeToggled = when(systemData.toggle)
        {
            "TRUE" -> "Yes"
            "FALSE" -> "No"
            else -> "No"
        }
        var type = ""
        if (systemDescriptionsData != null)
        {
            type = systemDescriptionsData.text2
        }

        var generalData = ""
        generalData += "Name: ``${systemData.name}``\n"
        generalData += "ID: ``${systemData.id}``\n"
        if (type != "") generalData += "Type: ``$type``\n"
        if (systemData.active != "") generalData += "Active for: ``${systemData.active}s``\n"
        if (systemData.cooldown != "") generalData += "Cooldown: ``${systemData.cooldown}s``\n"
        generalData += "Can be toggled: ``$canBeToggled``\n"

        //Do the response to the command
        val response = when(private)
        {
            true -> interaction.deferEphemeralResponse()
            false -> interaction.deferPublicResponse()
            else -> interaction.deferPublicResponse()
        }
        response.respond {

            embed {
                title = "Shipsystem: ${systemData.name}"
                if (systemDescriptionsData != null)
                {
                    description = systemDescriptionsData.text1.trimAfter(500)
                }

                if (generalData != "")
                field {
                    name = "General Data\n"
                    value = generalData
                    inline = true
                }

                footer {
                    this.text = "Loaded from ${modData.name} (V${modData.version})"
                }

                this.color = Color(10, 50, 155)
            }

            if (!private)
            {
                CommandUtil.addDeleteButton(this, interaction)
            }
        }
    }
}