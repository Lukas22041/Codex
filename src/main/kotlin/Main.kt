import bot.BotMain
import data.Loader
import java.io.File
import kotlin.system.measureTimeMillis

suspend fun main(args: Array<String>) {

    val timeInMillis = measureTimeMillis {
        for (file in File("data/").listFiles()!!) {
            if (file.isDirectory) {
                var test = file.name
                Loader().load("data/" + file.name)

                continue
            }
        }
    }

    println("\nFinished Loading Data in ${timeInMillis}ms")
    BotMain().init()
}




