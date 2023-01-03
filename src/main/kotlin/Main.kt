import bot.BotMain
import database.LoadedData
import database.Loader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.csv.CsvConfiguration
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.measureTimeMillis

suspend fun main(args: Array<String>) {

    val timeInMillis = measureTimeMillis {
        for (file in File("database/").listFiles()!!) {
            if (file.isDirectory) {
                var test = file.name
                Loader().load("database/" + file.name)

                continue
            }
        }
    }

    println("\nFinished Loading Data in ${timeInMillis}ms")
    BotMain().init()
}




