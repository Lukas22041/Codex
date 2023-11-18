package bot.commands

import bot.util.BaseCommand
import bot.util.CommandUtil
import bot.util.CommandUtil.addDeleteButton
import bot.util.CommandUtil.getFuzzyHullmod
import bot.util.CommandUtil.trimAfter
import data.LoadedData
import dev.kord.common.Color
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.embed

class ShowHullmod : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("hullmod", "name or id of the weapon") { required = true}
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        //Look for Mod and Weapon data
        val command = interaction.command
        //val modInput = command.strings["source"]!!
        val modInput = "Starsector"
        val hullmodInput = command.strings["hullmod"]!!
        var private = false
        try {
            private = command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        var modData = CommandUtil.loadModData(modInput, interaction) ?: return

        if (LoadedData.LoadedHullmodData.get(modData.id).isNullOrEmpty())
        {
            interaction.deferEphemeralResponse().respond { content = "Requested mod \"${modData.name}\" has no hullmods." }
            return
        }
        var hullmodData = LoadedData.LoadedHullmodData.get(modData.id)!!.find { it.id.lowercase() == hullmodInput.lowercase() || it.name.lowercase() == hullmodInput.lowercase() }
        if (hullmodData == null) hullmodData = getFuzzyHullmod(modData.id, hullmodInput)
        if (hullmodData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Unable to find hullmod going by \"$hullmodInput\" in ${modData.name}" }
            return
        }

        //Setup general data required for the card
        var generalData = ""
        generalData += "Name: ``${hullmodData.name}``\n"
        generalData += "ID: ``${hullmodData.id}``\n"
        generalData += "Ordnance Points: ``${hullmodData.cost_frigate}/${hullmodData.cost_dest}/${hullmodData.cost_cruiser}/${hullmodData.cost_capital}``\n"
        if (hullmodData.uiTags != "") generalData += "Category: ``${hullmodData.uiTags}``\n"

        //Do the response to the command
        val response = when(private)
        {
            true -> interaction.deferEphemeralResponse()
            false -> interaction.deferPublicResponse()
            else -> interaction.deferPublicResponse()
        }
        response.respond {

            embed {
                title = "Hullmod: ${hullmodData.name}"

                if (hullmodData.short != "")
                description = hullmodData.short.trimAfter(500)

                if (generalData != "")
                field {
                    name = "General Data\n"
                    value = generalData
                }

                footer {
                    this.text = "Loaded from ${modData.name} (V${modData.version})"
                }

                this.color = Color(10, 50, 155)
            }

            if (!private)
            {
                addDeleteButton(this, interaction)
            }
        }
    }
}