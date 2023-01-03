package data.loading

import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import kotlinx.serialization.json.*
import java.io.File

class LoadShipData(var basepath: String, var modID: String)
{
    @Serializable
    private data class ShipCSVData(var name: String, var id: String, var designation: String, @SerialName("system id") var systemID: String, @SerialName("tech/manufacturer") var tech: String)
    @Serializable
    private data class ShipJsonData(var hullId: String, var hullSize: String, var builtInMods: JsonArray? = null, var weaponSlots: JsonArray? = null)

    private var CSVData: List<ShipCSVData> = ArrayList()
    private var JsonData: MutableList<ShipJsonData> = ArrayList()

    fun load()
    {
        loadCSV()

        loadJson()

        combine()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        val file = File(basepath + DataPath.ShipCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true)
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
                try {
                    var data = Json { this.encodeDefaults = true; ignoreUnknownKeys = true}.decodeFromString<ShipJsonData>(ShipJsonData.serializer(), file.readText())
                    JsonData.add(data)
                    //println(data)
                }
                catch (e: Throwable)
                {
                    println("Error loading ${file.name}. Skipping.")
                }
            }
        }
    }

    private fun combine()
    {
        for (csv in CSVData)
        {
            var json: ShipJsonData? = JsonData.find { json -> csv.id == json.hullId } ?: continue

            var weaponSlots: MutableMap<String, Int> = HashMap()
            if (json!!.weaponSlots != null)
            {
                for (weapon in json.weaponSlots!!)
                {
                    var type = (weapon.jsonObject.get("type").toString()).replace("\"", "").trim().lowercase().capitalize()
                    var size = weapon.jsonObject.get("size").toString().replace("\"", "").trim().lowercase().capitalize()

                    var text = "$size-$type"
                    weaponSlots.put(text, weaponSlots.get(text)?.plus(1) ?: 1)
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
            builtInMods = builtinmods, weaponSlots = weaponSlots, systemID = csv.systemID)

            LoadedData.LoadedShipData.getOrPut(modID) { mutableListOf(data) }.add(data)
        }
        println("Loaded Ships for $modID")
    }
}