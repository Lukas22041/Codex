package bot.commands

import bot.ButtonData
import bot.util.BaseCommand
import bot.util.CommandUtil.getFuzzyHullmod
import bot.util.CommandUtil.getFuzzyMod
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

class ShowHullmod : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true}
            string("hullmod", "name or id of the weapon") { required = true}
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        //Look for Mod and Weapon data
        val command = interaction.command
        val modInput = command.strings["source"]!!
        val hullmodInput = command.strings["hullmod"]!!
        var private = false
        try {
            private = command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        var modData = LoadedData.LoadedModData.find { it.id.lowercase() == modInput.lowercase() || it.name.lowercase() == modInput.lowercase() }
        if (modData == null) modData = getFuzzyMod(modInput)
        if (modData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Unable to find mod \"$modInput\" in the bots database. Use /codex to look for available mods." }
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
                val emote = ReactionEmoji.Unicode("❌")
                actionRow {
                    var userdata = ButtonData(interaction.user.data.id.value, "delete_post")
                    this.interactionButton(ButtonStyle.Primary,  Json.encodeToString(ButtonData.serializer(), userdata)) {
                        this.label = "Delete"

                        emoji(emote)
                    }
                }
            }
        }
    }
}