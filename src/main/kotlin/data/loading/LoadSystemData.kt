package data.loading

import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import java.io.File

class LoadSystemData(var basepath: String, var modID: String)
{

    fun load()
    {
        loadCSV()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        val file = File(basepath + DataPath.ShipSystemsCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true)
        val csv = Csv(config)

        var data = csv.decodeFromString(ListSerializer(ShipsystemData.serializer()), file.readText())
        LoadedData.LoadedShipsystemData.put(modID, data.toMutableList())
        println("Loaded Shipsystems for $modID")
    }
}