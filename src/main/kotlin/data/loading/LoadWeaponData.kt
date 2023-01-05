package data.loading

import data.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import kotlinx.serialization.json.*
import java.io.File

class LoadWeaponData(var basepath: String, var modID: String)
{

    var text = ""
    @Serializable
    data class JsonWeaponData(var id: String, var type: String, var size: String)

    fun load()
    {
        loadCSV()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadCSV()
    {
        val file = File(basepath + DataPath.WeaponCSV)
        val config = CsvConfiguration(ignoreEmptyLines = true, ignoreUnknownColumns = true, hasHeaderRecord = true, recordSeparator = "\n")
        val csv = Csv(config)

        var data = csv.decodeFromString(ListSerializer(WeaponData.serializer()), file.readText())
        loadJson(data)
        //LoadedData.LoadedWeaponData.put(modID, data.toMutableList())
    }

    private fun loadJson(csvData: List<WeaponData>)
    {
        var jsonData: MutableList<JsonWeaponData> = ArrayList()
        for (file in File(basepath + DataPath.WeaponFolder).listFiles()!!) {
            if (file.extension == "wpn")
            {
                text = file.readText()
                while (text.indexOf('#') != -1) removeComments()
                val strb: StringBuilder = StringBuilder(text)
                val index = strb.lastIndexOf(",")
                strb.replace(index, ",".length + index, "")
                text = strb.toString()

                try {
                    var data = Json { this.encodeDefaults = true; ignoreUnknownKeys = true}.decodeFromString<JsonWeaponData>(JsonWeaponData.serializer(), text)
                    jsonData.add(data)
                }
                catch (e: Throwable)
                {
                    println("Error loading ${file.name}. Skipping.")
                }
            }
        }

        for (csv in csvData)
        {
            var json: JsonWeaponData? = jsonData.find { json -> csv.id == json.id }
            if (json != null)
            {
                csv.type = json!!.type
                csv.size = json.size
            }
            LoadedData.LoadedWeaponData.getOrPut(modID) { mutableListOf(csv) }.add(csv)
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
}