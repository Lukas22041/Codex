package bot.util

import bot.ButtonData
import data.*
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

    fun getFuzzyWeapon(source: String, weaponIdentifier: String) : WeaponData?
    {
        println("\nAttempting Fuzzy Search for Weapon with $weaponIdentifier\n")
        var currentRatio = 0
        var weapon: WeaponData? = null
        for (data in LoadedData.LoadedWeaponData.get(source)!!)
        {
            var ratio = FuzzySearch.ratio(weaponIdentifier, data.name)
            println("Fuzzy: ${data.name}/$ratio")
            if (ratio > currentRatio && ratio >= 20)
            {
                currentRatio = ratio
                weapon = data
            }
        }
        println("Fuzzy Search decided to pick ${weapon!!.name}") ?: println("No weapon found from Fuzzy\n")

        return weapon
    }

    fun getFuzzyHullmod(source: String, hullmodIdentifier: String) : HullmodData?
    {
        println("\nAttempting Fuzzy Search for Hullmod with $hullmodIdentifier\n")
        var currentRatio = 0
        var hullmod: HullmodData? = null
        for (data in LoadedData.LoadedHullmodData.get(source)!!)
        {
            var ratio = FuzzySearch.ratio(hullmodIdentifier, data.name)
            println("Fuzzy: ${data.name}/$ratio")
            if (ratio > currentRatio && ratio >= 20)
            {
                currentRatio = ratio
                hullmod = data
            }
        }
        println("Fuzzy Search decided to pick ${hullmod!!.name}") ?: println("No hullmod found from Fuzzy\n")

        return hullmod
    }

    fun getFuzzyShipsystem(source: String, systemIdentifier: String) : ShipsystemData?
    {
        println("\nAttempting Fuzzy Search for Shipsystem with $systemIdentifier\n")
        var currentRatio = 0
        var shipsystem: ShipsystemData? = null
        for (data in LoadedData.LoadedShipsystemData.get(source)!!)
        {
            var ratio = FuzzySearch.ratio(systemIdentifier, data.name)
            println("Fuzzy: ${data.name}/$ratio")
            if (ratio > currentRatio && ratio >= 20)
            {
                currentRatio = ratio
                shipsystem = data
            }
        }
        println("Fuzzy Search decided to pick ${shipsystem!!.name}") ?: println("No shipsystem found from Fuzzy\n")

        return shipsystem
    }


}