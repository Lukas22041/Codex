package data.loading

import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import java.io.File

class LoadDescriptionData(var basepath: String, var modID: String)
{
    fun load()
    {
        loadCSV()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        //  \r\n
        val file = File(basepath + DataPath.DescriptionsCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true, recordSeparator = "\n")
        val csv = Csv(config)

        var data = csv.decodeFromString(ListSerializer(DescriptionsData.serializer()), file.readText())
        LoadedData.LoadedDescriptionData.put(modID, data.toMutableList())
    }
}