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
            }
        }
    }

    println("\nFinished Loading Data in ${timeInMillis}ms for ${LoadedData.LoadedModData.size} mods")

    var test1= LoadedData.LoadedShipData
    var test2= LoadedData.LoadedWeaponData
    var test3= LoadedData.LoadedHullmodData
    var test4= LoadedData.LoadedShipsystemData
    var test5= LoadedData.LoadedDescriptionData

    var test6= ""

    runBlocking { startBot() }
}

suspend fun startBot()
{
    BotMain().init()
}




