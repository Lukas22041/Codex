package bot.commands

import bot.ButtonData
import bot.util.BaseCommand
import bot.util.CommandUtil.getFuzzyMod
import bot.util.CommandUtil.getFuzzyShip
import data.HullmodData
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

class ShowShip : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true}
            string("ship", "name or id of the ship") { required = true}
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        val command = interaction.command
        val source = command.strings["source"]!!
        val shipIdentifier = command.strings["ship"]!!
        var moddata = LoadedData.LoadedModData.find { it.id == source || it.name == source } ?: getFuzzyMod(source)

        if (moddata == null)
        {
            interaction.deferEphemeralResponse().respond { content = "No Mod found by that ID." }
        }
        else
        {
            interaction.deferPublicResponse().respond {
                var ships = LoadedData.LoadedShipData.get(moddata.id)
                var ship = ships!!.find { it.id == shipIdentifier || it.name == shipIdentifier } ?: getFuzzyShip(moddata.id, shipIdentifier)

                if (ship == null)
                {
                    interaction.deferEphemeralResponse().respond { content = "Request Ship could not be found under that source, id or name" }
                }
                else
                {
                    var shipsystemData = LoadedData.LoadedShipsystemData.get(moddata.id)!!.find { it.id == ship.systemID }
                    var hullmodsDataList: MutableList<HullmodData> = ArrayList()

                    var hullmods = ""
                    if (!ship.builtInMods.isNullOrEmpty())
                    {
                        for (hullmod in ship.builtInMods!!)
                        {
                            var data = LoadedData.LoadedHullmodData.get(moddata.id)!!.find { it.id == hullmod }
                            if (data != null)
                            {
                                hullmods += "``${data.name}``\n"
                            }
                        }
                    }

                    var slots = ""
                    if (!ship.weaponSlots.isNullOrEmpty())
                    {
                        for (weapon in ship.weaponSlots.toSortedMap()!!)
                        {
                            slots += "``${weapon.key} x${weapon.value}``\n"
                        }
                    }

                    embed {
                        title = "Ship: ${ship.name}"
                        var shipDescription = LoadedData.LoadedDescriptionData.get(moddata.id)!!.find { it.id == ship.id }
                        if (shipDescription != null)
                        {
                            description = shipDescription.text1
                        }

                        field {
                            name = "General Data\n"
                            value = "Name: ``${ship.name}``\n" +
                                    "ID: ``${ship.id}``\n" +
                                    "Hullsize: ``${ship.hullSize.replace("_", " ").lowercase().capitalize()}``\n" +
                                    "OP: ``${ship.ordnancePoints}``\n" +
                                    "DP: ``${ship.deploymentPoints}``\n"
                            inline = true
                        }

                        var stats = ""
                        stats += "Armor Rating: ``${ship.armorRating}``\n"
                        stats += "Hitpoints Rating: ``${ship.hitpoints}``\n"
                        stats += "Max Flux: ``${ship.maxFlux}``\n"
                        stats += "Flux Dissipation: ``${ship.fluxDissipation}``\n"
                        stats += "Max Speed: ``${ship.maxSpeed}``\n"
                        stats += "Shield Type: ``${ship.shieldType.lowercase().capitalize()}``\n"
                        if (ship.shieldArc != "" && ship.shieldArc != "0") stats += "Shield Arc: ``${ship.shieldArc}``\n"
                        if (ship.shieldEfficiency != "" && ship.shieldEfficiency != "0") stats += "Shield Efficiency: ``${ship.shieldEfficiency}``\n"
                        if (ship.fighterBays != "") stats += "Fighter Bays**: ``${ship.fighterBays}``\n"


                        field {
                            name = "Stats"
                            value = stats
                            inline = true
                        }

                        var desc = LoadedData.LoadedDescriptionData.get(moddata.id)!!.find { it.id == shipsystemData!!.id }
                        if (desc != null && desc.text1 != "")
                        {
                            var description = desc!!.text1
                            field { name = "Shipsystem: ${shipsystemData!!.name}"; value = "``$description``" }
                        }

                        if (slots != "")
                        {
                            field { name = "Weapon Slots"; value = "\n$slots"; this.inline = true}
                        }
                        if (hullmods != "")
                        {
                            field { name = "Hullmods"; value = "\n$hullmods"; this.inline = true}
                        }

                        footer {
                            this.text = "Loaded from ${moddata!!.name} (V${moddata.version})"
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
    }
}