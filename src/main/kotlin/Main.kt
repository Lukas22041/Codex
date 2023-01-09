import bot.BotMain
import data.LoadedData
import data.Loader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {

    if (!File("database/").exists()) File("database/").mkdir()
    val timeInMillis = measureTimeMillis {
        for (file in File("database/").listFiles()!!) {
            if (file.isDirectory) {
                Loader().load("database/" + file.name)
            }
        }
    }

    var test = LoadedData.LoadedShipData
    var test2 = LoadedData.LoadedWeaponData
    var test3 = LoadedData.LoadedHullmodData
    var test4 = LoadedData.LoadedShipsystemData

    var test6 = "LoadedData.LoadedWeaponData"

    println("\nFinished Loading Data in ${timeInMillis}ms for ${LoadedData.LoadedModData.size} mods")
    runBlocking(Dispatchers.Default) { BotMain().init() }
}




