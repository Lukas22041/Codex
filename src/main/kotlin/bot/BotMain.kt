package bot

import bot.commands.CodexInfo
import data.HullmodData
import data.LoadedData
import data.ModData
import data.ShipData
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.xdrop.fuzzywuzzy.FuzzySearch

var token = ClassLoader.getSystemResource("bot-token.txt").readText().trim()

class BotMain
{
    suspend fun init()
    {
        var kord = Kord(token) {

        }

        //Load Commands
        CodexInfo().init(kord, "codex-info", "Show Codex Bot Information")

        kord.createGlobalChatInputCommand("ship", "Displays ship data") {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true}
            string("ship", "name or id of the ship") { required = true}
        }

        kord.on<ChatInputCommandInteractionCreateEvent > {
            val command = interaction.command

            when (command.rootName)
            {

                "ship" ->
                {

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
                                    var shipDescription = LoadedData.LoadedDescriptionData.get(moddata.id)!!.find { it.id == ship.id }
                                    if (shipDescription != null)
                                    {
                                        description = shipDescription.text1
                                    }

                                    field {
                                        name = "General Data\n"
                                        value = "**Name**: ``${ship.name}``\n" +
                                                "**ID**: ``${ship.id}``\n" +
                                                "**Hullsize**: ``${ship.hullSize.lowercase().capitalize()}``"
                                    }

                                    var desc = LoadedData.LoadedDescriptionData.get(moddata.id)!!.find { it.id == shipsystemData!!.id }
                                    if (desc != null)
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
        }

        kord.on<GuildButtonInteractionCreateEvent> {
            var response = interaction.deferEphemeralResponse()

            var buttonData: ButtonData = Json.decodeFromString(ButtonData.serializer(), interaction.componentId)

            if (buttonData.buttonID == "delete_post")
            {
                response.respond {
                    println(interaction.message.asMessage().data.author.id.value)
                    println(interaction.user.memberData.userId.value)

                    if (buttonData.user == interaction.user.memberData.userId.value)
                    {
                        content = "Removed Message"
                        interaction.message.delete("Deleted by user request")
                    }
                    else
                    {
                        content = "Lacking permissions to delete message"
                    }
                }
            }
        }

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
            println("\nBot Started")
        }
    }

    fun getFuzzyShip(source: String, shipIdentifier: String) : ShipData?
    {
        println("\nAttempting Fuzzy Search for Ship with $shipIdentifier\n")
        var currentRatio = 0
        var ship: ShipData? = null
        for (data in LoadedData.LoadedShipData.get(source)!!)
        {
            var ratio = FuzzySearch.ratio(shipIdentifier, data.name)
            println("Fuzzy: ${data.name}/$ratio")
            if (ratio > currentRatio && ratio >= 20)
            {
                currentRatio = ratio
                ship = data
            }
        }
        println("Fuzzy Search decided to pick ${ship!!.name}") ?: println("No ship found from Fuzzy\n")

        return ship
    }

    fun getFuzzyMod(mod: String) : ModData?
    {
        println("\nAttempting Fuzzy Search for Ship with $mod")
        var currentRatio = 0
        var modData: ModData? = null

        for (data in LoadedData.LoadedModData)
        {
            var ratio = FuzzySearch.ratio(mod, data.name)
            println("Ratio: ${data.name}/$ratio")
            if (ratio > currentRatio && ratio >= 20)
            {
                currentRatio = ratio
                modData = data
            }
        }

        if (modData != null)
        {
            println("Fuzzy Search decided to pick mod ${modData!!.name}\n")
        }
        else
        {
             println("No mod found from Fuzzy Search\n")
        }

        return modData
    }
}

@Serializable
data class ButtonData(val user: ULong, var buttonID: String)