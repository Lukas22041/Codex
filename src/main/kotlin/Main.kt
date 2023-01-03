import bot.BotMain
import data.LoadedData
import data.Loader
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




