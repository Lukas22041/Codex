package bot.commands

import bot.ButtonData
import bot.util.BaseCommand
import bot.util.CommandUtil.getFuzzyHullmod
import bot.util.CommandUtil.getFuzzyMod
import data.LoadedData
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
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
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        //Look for Mod and Weapon data
        val command = interaction.command
        val modInput = command.strings["source"]!!
        val hullmodInput = command.strings["hullmod"]!!

        var modData = LoadedData.LoadedModData.find { it.id == modInput || it.name == modInput } ?: getFuzzyMod(modInput)
        if (modData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Could not find mod going by \"$modInput\"." }
            return
        }

        var hullmodData = LoadedData.LoadedHullmodData.get(modData.id)!!.find { it.id == hullmodInput || it.name == hullmodInput } ?: getFuzzyHullmod(modData.id, hullmodInput)
        if (hullmodData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "No Hullmod found in ${modData.name} by that id or name." }
            return
        }

        //Setup general data required for the card
        var generalData = ""
        generalData += "Name: ``${hullmodData.name}``\n"
        generalData += "ID: ``${hullmodData.id}``\n"
        generalData += "Ordnance Points: ``${hullmodData.cost_frigate}/${hullmodData.cost_dest}/${hullmodData.cost_cruiser}/${hullmodData.cost_capital}``\n"
        generalData += "Category: ``${hullmodData.uiTags}``\n"

        //Do the response to the command
        interaction.deferPublicResponse().respond {

            embed {
                title = "Hullmod: ${hullmodData.name}"
                    description = hullmodData.short

                field {
                    name = "General Data\n"
                    value = generalData
                }

                footer {
                    this.text = "Loaded from ${modData.name} (V${modData.version})"
                }

                this.color = Color(10, 50, 155)
            }

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