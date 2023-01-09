package bot.util

import bot.ButtonData
import data.*
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.util.*

@Serializable
data class ButtonData(val user: ULong, var buttonID: String)

object CommandUtil
{
    fun buttonDataToJson(data: ButtonData) : String
    {
        return Json.encodeToString(ButtonData.serializer(), data)
    }

    fun buttonJsonToData(data: String) : ButtonData
    {
        return Json.decodeFromString(ButtonData.serializer(), data)
    }

    suspend fun loadModData(modInput: String, interaction: ChatInputCommandInteraction) : ModData?
    {
        var modData = LoadedData.LoadedModData.find { it.id.lowercase() == modInput.lowercase() || it.name.lowercase() == modInput.lowercase() }
        if (modData == null) modData = getFuzzyMod(modInput)

        if (modData == null) interaction.deferEphemeralResponse().respond {
            content = "Unable to find mod \"$modInput\" in the bot's database. Mods are only included by the author's request. Use /codex to search available mods." }

        return modData
    }

    fun getFuzzyMod(mod: String) : ModData?
    {
        println("\nAttempting Fuzzy Search for Ship with $mod")
        var currentRatio = 0
        var modData: ModData? = null

        for (data in LoadedData.LoadedModData)
        {
            var ratio = FuzzySearch.extractOne(mod, listOf(data.id, data.name, data.name.abbreviate()))
            if (ratio.score > currentRatio && ratio.score >= 70)
            {
                currentRatio = ratio.score
                modData = data
            }
        }

        if (modData != null)
        {
            println("Fuzzy Search decided to pick mod ${modData.name} with confidence $currentRatio\n")
        }
        else
        {
            println("No mod found from Fuzzy Search\n")
        }

        return modData
    }

    fun getFuzzyShip(source: String, shipIdentifier: String) : ShipData?
    {
        println("\nAttempting Fuzzy Search for Ship with $shipIdentifier")
        var currentRatio = 0
        var ship: ShipData? = null
        for (data in LoadedData.LoadedShipData.get(source)!!)
        {
            var ratio = FuzzySearch.extractOne(shipIdentifier, listOf(data.id, data.name))
            if (ratio.score > currentRatio && ratio.score >= 50)
            {
                currentRatio = ratio.score
                ship = data
            }
        }
        if (ship != null) println("Fuzzy Search decided to pick ship ${ship.id} with confidence $currentRatio\n") else println("No ship found from Fuzzy\n")

        return ship
    }

    fun getFuzzyWeapon(source: String, weaponIdentifier: String) : WeaponData?
    {
        println("\nAttempting Fuzzy Search for Weapon with $weaponIdentifier")
        var currentRatio = 0
        var weapon: WeaponData? = null
        for (data in LoadedData.LoadedWeaponData.get(source)!!)
        {
            var ratio = FuzzySearch.extractOne(weaponIdentifier, listOf(data.id, data.name))
            if (ratio.score > currentRatio && ratio.score >= 50)
            {
                currentRatio = ratio.score
                weapon = data
            }
        }
        if (weapon != null) println("Fuzzy Search decided to weapon ship ${weapon.id} with confidence $currentRatio\n") else println("No weapon found from Fuzzy\n")

        return weapon
    }

    fun getFuzzyHullmod(source: String, hullmodIdentifier: String) : HullmodData?
    {
        println("\nAttempting Fuzzy Search for Hullmod with $hullmodIdentifier")
        var currentRatio = 0
        var hullmod: HullmodData? = null
        for (data in LoadedData.LoadedHullmodData.get(source)!!)
        {
            var ratio = FuzzySearch.extractOne(hullmodIdentifier, listOf(data.id, data.name))
            if (ratio.score > currentRatio && ratio.score >= 50)
            {
                currentRatio = ratio.score
                hullmod = data
            }
        }
        if (hullmod != null) println("Fuzzy Search decided to hullmod ship ${hullmod.id} with confidence $currentRatio\n") else println("No hullmod found from Fuzzy\n")

        return hullmod
    }

    fun getFuzzyShipsystem(source: String, systemIdentifier: String) : ShipsystemData?
    {
        println("\nAttempting Fuzzy Search for Shipsystem with $systemIdentifier")
        var currentRatio = 0
        var shipsystem: ShipsystemData? = null
        for (data in LoadedData.LoadedShipsystemData.get(source)!!)
        {
            var ratio = FuzzySearch.extractOne(systemIdentifier, listOf(data.id, data.name))
            if (ratio.score > currentRatio && ratio.score >= 50)
            {
                currentRatio = ratio.score
                shipsystem = data
            }
        }
        if (shipsystem != null) println("Fuzzy Search decided to shipsystem ship ${shipsystem.id} with confidence $currentRatio\n") else println("No shipsystem found from Fuzzy\n")

        return shipsystem
    }

    fun addDeleteButton(response: InteractionResponseModifyBuilder, interaction: ChatInputCommandInteraction)
    {
        /*val emote = ReactionEmoji.Unicode("❌")
        response.actionRow {
            var userdata = ButtonData(interaction.user.data.id.value, "delete_post")
            this.interactionButton(ButtonStyle.Secondary,  Json.encodeToString(ButtonData.serializer(), userdata)) {
                this.label = "Delete"
                emoji(emote)
            }
        }*/
    }

    /**Removes any character after the cap*/
    fun String.trimAfter(cap: Int) : String
    {
        return if (this.length <= cap)
        {
            this
        }
        else
        {
            this.substring(0, cap).trim() + "... (Cutoff)"
        }
    }

    fun String.abbreviate() : String
    {
        return this.replace("\\B.|\\P{L}".toRegex(), "").lowercase(Locale.getDefault())
    }
}