package data.loading

import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import java.io.File

class LoadHullmodsData(var basepath: String, var modID: String)
{

    fun load()
    {
        loadCSV()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        val file = File(basepath + DataPath.HullmodsCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true, recordSeparator = "\r")
        val csv = Csv(config)

        var test =file.readText()
        var test2 = ""
        var data = csv.decodeFromString(ListSerializer(HullmodData.serializer()), file.readText())
        LoadedData.LoadedHullmodData.put(modID, data.toMutableList())
        println("Loaded hullmods for $modID")
    }
}