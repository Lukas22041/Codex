import bot.BotMain
import data.LoadedData
import data.Loader
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

    if (!File("database/").exists()) File("database/").mkdir()
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

    var test = LoadedData.LoadedHullmodData

    var test2 = ""

    runBlocking { startBot() }

}

suspend fun startBot()
{
    BotMain().init()
}




