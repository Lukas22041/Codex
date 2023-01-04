package data.loading

import data.DataPath
import data.LoadedData
import data.ModData
import kotlinx.serialization.json.Json
import java.io.File

class LoadModData
{

    var text = ""

    fun load(basefolder: String) : ModData?
    {
        if (basefolder == "database/starsector-core")
        {
            var data = ModData("starsector", "Starsector", "0.95.1a")
            LoadedData.LoadedModData.add(data)
            return data
        }
        else
        {
            text = File(basefolder + DataPath.ModInfo).readText()

            //Remove trialing comma
            val strb: StringBuilder = StringBuilder(text)
            val index = strb.lastIndexOf(",")
            strb.replace(index, ",".length + index, "")
            text = strb.toString()

            while (text.indexOf('#') != -1) removeComments()

            var data: ModData? = null
            try {
                var data = Json { ignoreUnknownKeys = true}.decodeFromString<ModData>(ModData.serializer(), text)
                LoadedData.LoadedModData.add(data)
                return data
            }
            catch(e: Throwable)
            {
                println(text)
                println(e.printStackTrace())
            }

            return data
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