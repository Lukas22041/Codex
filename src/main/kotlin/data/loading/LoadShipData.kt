package data.loading

import data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import kotlinx.serialization.json.*
import java.awt.Color
import java.io.File

class LoadShipData(var basepath: String, var modID: String)
{
    @Serializable
    private data class ShipCSVData(var name: String, var id: String, var designation: String, @SerialName("system id") var systemID: String, @SerialName("tech/manufacturer") var tech: String,
                                   @SerialName("ordnance points") var ordnancePoints: String, @SerialName("supplies/rec") var deploymentPoints: String, var hitpoints: String,
                                   @SerialName("armor rating") var armorRating: String, @SerialName("max flux") var maxFlux: String, @SerialName("flux dissipation") var fluxDissipation: String,
                                   @SerialName("fighter bays") var fighterBays: String, @SerialName("max speed") var maxSpeed: String, @SerialName("shield type") var shieldType: String,
                                   @SerialName("shield arc") var shieldArc: String, @SerialName("shield efficiency") var shieldEfficiency: String)
    @Serializable
    private data class ShipJsonData(var hullId: String, var hullSize: String, var builtInMods: JsonArray? = null, var weaponSlots: JsonArray? = null)

    @Serializable
    private data class SkinJsonData(var skinHullId: String = "", var baseHullId: String = "", var hullName: String = "", var descriptionId: String = "", var descriptionPrefix: String = "",
                                    var fleetPoints: String = "", var ordnancePoints: String = "", var removeWeaponSlots: JsonArray? = null, var removeEngineSlots: JsonArray? = null,
                                    var removeBuiltInMods: JsonArray? = null, var removeBuiltInWeapons: JsonArray? = null, var builtInMods: JsonArray? = null, var weaponSlots: JsonArray? = null)

    private var CSVData: List<ShipCSVData> = ArrayList()
    private var JsonData: MutableList<ShipJsonData> = ArrayList()
    var text = ""

    fun load()
    {
        runBlocking {
            launch(Dispatchers.IO) { loadCSV() }
            launch(Dispatchers.IO) { loadJson() }
        }

        combine()

        loadSkinJson()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        val file = File(basepath + DataPath.ShipCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true, recordSeparator = "\n")
        val csv = Csv(config)


        CSVData = csv.decodeFromString(ListSerializer(ShipCSVData.serializer()), file.readText())
        //println(CSVData)
    }

    private fun loadJson()
    {
        var jsonList: MutableList<ShipJsonData> = ArrayList()
        for (file in File(basepath + DataPath.ShipFolder).listFiles()!!) {
            if (file.extension == "ship")
            {
                text = file.readText()
                while (text.indexOf('#') != -1) removeComments()
                val strb: StringBuilder = StringBuilder(text)
                val index = strb.lastIndexOf(",")
                strb.replace(index, ",".length + index, "")
                text = strb.toString()

                try {
                    var data = Json { this.encodeDefaults = true; ignoreUnknownKeys = true}.decodeFromString<ShipJsonData>(ShipJsonData.serializer(), text)
                    JsonData.add(data)
                }
                catch (e: Throwable)
                {
                    println("Error loading ${file.name}. Skipping.")
                }
            }
        }
    }

    private fun loadSkinJson()
    {
        var jsonList: MutableList<ShipJsonData> = ArrayList()

        var files = File(basepath + DataPath.SkinFolder).listFiles()!!.asList().toMutableList()
        var moreFiles: MutableList<File> = ArrayList()
        for (file in files)
        {
            if (file.isDirectory) files.addAll(file.listFiles()!!)
        }

        var ships = LoadedData.LoadedShipData.flatMap { it.value }

        for (file in files) {
            if (file.extension == "skin")
            {
                text = file.readText()
                while (text.indexOf('#') != -1) removeComments()
                val strb: StringBuilder = StringBuilder(text)
                val index = strb.lastIndexOf(",")
                strb.replace(index, ",".length + index, "")
                text = strb.toString()

                try {
                    var data = Json { this.encodeDefaults = true; ignoreUnknownKeys = true; this.isLenient = true}.decodeFromString<SkinJsonData>(SkinJsonData.serializer(), text)
                    var ship: ShipData = (ships.find { it.id == data.baseHullId } ?: continue).copy()

                    ship.id = data.skinHullId
                    ship.baseHull = data.baseHullId
                    ship.name = data.hullName
                    if (data.fleetPoints != "") ship.deploymentPoints = data.fleetPoints
                    if (data.ordnancePoints != "") ship.ordnancePoints = data.ordnancePoints

                    var removeWeaponSlots: List<String>? = null
                    if (data.removeWeaponSlots != null) removeWeaponSlots = data.removeWeaponSlots!!.map { it.toString() }

                    var removeInbuiltHullmods: List<String>? = null
                    if (data.removeBuiltInMods != null) removeInbuiltHullmods = data.removeBuiltInMods!!.map { it.toString() }

                    var newWeaponSlots: MutableList<WeaponSlot> = ArrayList()
                    for (slot in ship.weaponSlots)
                    {
                        if (removeWeaponSlots == null || removeWeaponSlots.none { remove -> remove.replace("\"", "").trim().lowercase().capitalize().contains(slot.id) })
                        {
                            newWeaponSlots.add(slot)
                        }
                    }

                    var builtinmods: MutableList<String> = ArrayList()
                    if (data.builtInMods != null)
                    {
                        for (mod in data.builtInMods!!)
                        {
                            builtinmods.add(mod.toString().replace("\"", "").trim())
                        }
                    }

                    var newHullmods: MutableList<String> = ArrayList()
                    if (builtinmods.isNotEmpty()) newHullmods.addAll(builtinmods)
                    for (mod in ship.builtInMods!!)
                    {
                        if (removeInbuiltHullmods == null || removeInbuiltHullmods.none { remove -> remove.replace("\"", "").trim().contains(mod)})
                        {
                            newHullmods.add(mod)
                        }
                    }

                    ship.weaponSlots = newWeaponSlots
                    ship.builtInMods = newHullmods
                    ship.skinDescription = data.descriptionPrefix

                    var test: Map<String, Int> = HashMap()
                    for ((key, value) in test)
                    {

                    }

                    LoadedData.LoadedShipData.get(modID)!!.add(ship)
                }
                catch (e: Throwable)
                {
                    println("Error loading ${file.name}. Skipping.")
                    //println(e.printStackTrace())
                    //println(text)
                }
            }
        }
    }

    private fun combine()
    {
        for (csv in CSVData)
        {
            var json: ShipJsonData? = JsonData.find { json -> csv.id == json.hullId } ?: continue

            var weaponSlots: MutableList<WeaponSlot> = ArrayList()
            if (json!!.weaponSlots != null)
            {
                for (slot in json.weaponSlots!!)
                {
                    if (slot.jsonObject.get("mount").toString().lowercase().contains("hidden")) continue
                    var type = (slot.jsonObject.get("type").toString()).replace("\"", "").trim().lowercase().capitalize()
                    var size = slot.jsonObject.get("size").toString().replace("\"", "").trim().lowercase().capitalize()
                    var arc = slot.jsonObject.get("arc").toString().replace("\"", "").trim().lowercase().capitalize()
                    var id = slot.jsonObject.get("id").toString().replace("\"", "").trim().lowercase().capitalize()
                    var angle = slot.jsonObject.get("angle").toString().replace("\"", "").trim().lowercase().capitalize()
                    var mount = slot.jsonObject.get("mount").toString().replace("\"", "").trim().lowercase().capitalize()

                    weaponSlots.add(WeaponSlot(angle, arc, id, mount, size, type))
                }
            }

            var builtinmods: MutableList<String> = ArrayList()
            if (json!!.builtInMods != null)
            {
                for (mod in json.builtInMods!!)
                {
                    builtinmods.add(mod.toString().replace("\"", "").trim())
                }
            }

            var test = weaponSlots

            var data = ShipData(id = csv.id, name = csv.name, designation = csv.designation, tech = csv.tech, hullSize = json.hullSize,
            builtInMods = builtinmods, weaponSlots = weaponSlots.toList(), systemID = csv.systemID, deploymentPoints = csv.deploymentPoints, ordnancePoints = csv.ordnancePoints,
            hitpoints = csv.hitpoints, armorRating = csv.armorRating, maxFlux = csv.maxFlux, fluxDissipation = csv.fluxDissipation, fighterBays = csv.fighterBays, maxSpeed = csv.maxSpeed,
            shieldArc = csv.shieldArc, shieldType = csv.shieldType, shieldEfficiency = csv.shieldEfficiency)

            LoadedData.LoadedShipData.getOrPut(modID) { mutableListOf(data) }.add(data)
        }
    }

    fun removeComments()
    {

        var index = text.indexOf("#")
        if (index == -1) return


        if (text.contains("#"))
        {
            //if (index == -1) return
            for (i in index until text.length)
            {
                try {
                    if (text.substring(i, i+1) == "\n")
                    {

                        var toReplace = text.substring(index, i)
                        text = text.replace(toReplace, "")
                        //println(text)
                        return
                    }
                }
                catch (e: Throwable) {
                    //println("error")
                }
            }
        }
    }

    data class test(var test1: Int, var test2: String)
}