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

class LoadWeaponData(var basepath: String, var modID: String)
{



    fun load()
    {
        loadCSV()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        val file = File(basepath + DataPath.WeaponCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true)
        val csv = Csv(config)

        var data = csv.decodeFromString(ListSerializer(WeaponData.serializer()), file.readText())
        LoadedData.LoadedWeaponData.put(modID, data.toMutableList())
    }
}