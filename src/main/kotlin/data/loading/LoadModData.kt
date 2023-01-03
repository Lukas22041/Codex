package data.loading

import data.DataPath
import data.LoadedData
import data.ModData
import kotlinx.serialization.json.Json
import java.io.File

class LoadModData
{
    fun load(basefolder: String) : ModData
    {
        if (basefolder == "database/starsector-core")
        {
            var data = ModData("starsector", "Starsector", "0.95.1a")
            LoadedData.LoadedModData.add(data)
            return data
        }
        else
        {
            var data = Json { ignoreUnknownKeys = true}.decodeFromString<ModData>(ModData.serializer(), File(basefolder + DataPath.ModInfo).readText())
            LoadedData.LoadedModData.add(data)
            return data
        }
    }
}