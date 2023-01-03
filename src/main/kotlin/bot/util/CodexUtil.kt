package bot.util

import bot.ButtonData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
}