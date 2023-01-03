package bot.util

import bot.ButtonData
import data.LoadedData
import data.ModData
import data.ShipData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.xdrop.fuzzywuzzy.FuzzySearch

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