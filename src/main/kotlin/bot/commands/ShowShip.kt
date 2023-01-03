package bot.commands

import bot.ButtonData
import bot.util.BaseCommand
import bot.util.CommandUtil.getFuzzyMod
import bot.util.CommandUtil.getFuzzyShip
import data.DescriptionsData
import data.HullmodData
import data.LoadedData
import data.ShipsystemData
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

class ShowShip : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true}
            string("ship", "name or id of the ship") { required = true}
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        //Look for Mod and Ship data
        val command = interaction.command
        val modInput = command.strings["source"]!!
        val shipInput = command.strings["ship"]!!
        var private = false
        try {
            private = command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        var modData = LoadedData.LoadedModData.find { it.id == modInput || it.name == modInput } ?: getFuzzyMod(modInput)
        if (modData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Could not find mod going by \"$modInput\"." }
            return
        }

        var shipData = LoadedData.LoadedShipData.get(modData.id)!!.find { it.id == shipInput || it.name == shipInput } ?: getFuzzyShip(modData.id, shipInput)
        if (shipData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "No Ship found in ${modData.name} by that id or name." }
            return
        }

        //Setup general data required for the card
        var shipDescription = LoadedData.LoadedDescriptionData.get(modData.id)!!.find { it.id == shipData.id }
        var hullmodDataList: MutableList<HullmodData> = ArrayList()
        shipData.builtInMods!!.forEach {
            hullmodDataList.add(LoadedData.LoadedHullmodData.get(modData.id)!!.find { storedHullmod -> storedHullmod.id == it } ?: return@forEach)  }
        var shipsystemData = LoadedData.LoadedShipsystemData.get(modData.id)!!.find { it.id == shipData.systemID }
        var shipsystemDescription: DescriptionsData? = null
        if (shipsystemData != null) shipsystemDescription = LoadedData.LoadedDescriptionData.get(modData.id)!!.find { it.id == shipsystemData!!.id }

        var weaponSlots = ""
        if (!shipData.weaponSlots.isNullOrEmpty())
        {
            for (weapon in shipData.weaponSlots.toSortedMap()!!)
            {
                weaponSlots += "``${weapon.key} x${weapon.value}``\n"
            }
        }

        var hullmods = ""
        hullmodDataList.forEach {hullmod ->
            hullmods += "``${hullmod.name}``\n"
        }

        var generalData = ""
        generalData += "Name: ``${shipData.name}``\n"
        generalData += "ID: ``${shipData.id}``\n"
        generalData += "Hullsize: ``${shipData.hullSize.replace("_", " ").lowercase().capitalize()}``\n"
        generalData += "Ordnance Points: ``${shipData.ordnancePoints}``\n"
        generalData += "Deployment Points: ``${shipData.deploymentPoints}``\n"

        var stats = ""
        stats += "Armor Rating: ``${shipData.armorRating}``\n"
        stats += "Hitpoints Rating: ``${shipData.hitpoints}``\n"
        stats += "Max Flux: ``${shipData.maxFlux}``\n"
        stats += "Flux Dissipation: ``${shipData.fluxDissipation}``\n"
        stats += "Max Speed: ``${shipData.maxSpeed}``\n"
        stats += "Shield Type: ``${shipData.shieldType.lowercase().capitalize()}``\n"
        if (shipData.shieldArc != "" && shipData.shieldArc != "0") stats += "Shield Arc: ``${shipData.shieldArc}``\n"
        if (shipData.shieldEfficiency != "" && shipData.shieldEfficiency != "0") stats += "Shield Efficiency: ``${shipData.shieldEfficiency}``\n"
        if (shipData.fighterBays != "") stats += "Fighter Bays: ``${shipData.fighterBays}``\n"

        //Do the response to the command
        val response = when(private)
        {
            true -> interaction.deferEphemeralResponse()
            false -> interaction.deferPublicResponse()
            else -> interaction.deferPublicResponse()
        }
        response.respond {
            embed {
                title = "Ship: ${shipData.name}"
                if (shipDescription != null)
                {
                    description = shipDescription.text1
                }

                field {
                    name = "General Data\n"
                    value = generalData
                    inline = true
                }

                field {
                    name = "Stats"
                    value = stats
                    inline = true
                }
                if (shipsystemDescription != null && shipsystemDescription!!.text1 != "")
                {
                    field { name = "Shipsystem: ${shipsystemData!!.name}"; value = "``${shipsystemDescription.text1}``" }
                }

                if (weaponSlots != "")
                {
                    field { name = "Weapon Slots"; value = "\n$weaponSlots"; this.inline = true}
                }
                if (hullmods != "")
                {
                    field { name = "Hullmods"; value = "\n$hullmods"; this.inline = true}
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