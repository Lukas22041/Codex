package data

import data.loading.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class Loader()
{
    fun load(basefolder: String)
    {
        var mod = LoadModData().load(basefolder)

        println("\n\nLoading Data")
        println("Id: ${mod.id}")
        println("Name: ${mod.name}")
        println("Version: ${mod.version}")

        runBlocking {

            //Load Description
            launch(Dispatchers.IO) {
                try {
                    val timeInMillis = measureTimeMillis {LoadDescriptionData(basefolder, mod.id).load() }
                    println("Loaded ${LoadedData.LoadedDescriptionData.get(mod.id)!!.size} descriptions for ${mod.id} in ${timeInMillis}ms")
                }
                catch (e: Throwable)
                {
                    println("Failed to load Descriptions for ${mod.id}")
                    println(e.printStackTrace())
                }
            }
            //Load Ships
            launch(Dispatchers.IO) {
                try {
                    val timeInMillis = measureTimeMillis { LoadShipData(basefolder, mod.id).load()
                        var removalList: MutableList<ShipData> = ArrayList()
                        var list = LoadedData.LoadedShipData.get(mod.id)
                        list!!.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalList.add(data) }
                        LoadedData.LoadedShipData.get(mod.id)!!.removeAll(removalList) }

                    println("Loaded ${LoadedData.LoadedShipData.get(mod.id)!!.size} Ships for ${mod.id} in ${timeInMillis}ms")
                }
                catch (e: Throwable)
                {
                    println("Failed to load Ships for ${mod.id}")
                    println(e.printStackTrace())
                }
            }

            //Load Weapons
            launch(Dispatchers.IO) {
                try {
                    val timeInMillis = measureTimeMillis { LoadWeaponData(basefolder, mod.id).load()
                        var removalList: MutableList<WeaponData> = ArrayList()
                        var list = LoadedData.LoadedWeaponData.get(mod.id)
                        list!!.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalList.add(data) }
                        LoadedData.LoadedWeaponData.get(mod.id)!!.removeAll(removalList) }
                    println("Loaded ${LoadedData.LoadedWeaponData.get(mod.id)!!.size} weapons for ${mod.id} in ${timeInMillis}ms")
                }
                catch (e: Throwable)
                {
                    println("Failed to load Weapons for ${mod.id}")
                    println(e.printStackTrace())
                }
            }
            //Load Hullmods
            launch(Dispatchers.IO) {
                try {
                    val timeInMillis = measureTimeMillis { LoadHullmodsData(basefolder, mod.id).load()
                        var removalList: MutableList<HullmodData> = ArrayList()
                        var list = LoadedData.LoadedHullmodData.get(mod.id)
                        list!!.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalList.add(data) }
                        LoadedData.LoadedHullmodData.get(mod.id)!!.removeAll(removalList) }
                    println("Loaded hullmods for ${mod.id} in ${timeInMillis}ms")
                }
                catch (e: Throwable)
                {
                    println("Failed to load Hullmods for ${mod.id}")
                    println(e.printStackTrace())
                }
            }
            //Load Systems
            launch(Dispatchers.IO) {
                try {
                    val timeInMillis = measureTimeMillis { LoadSystemData(basefolder, mod.id).load()
                        var removalList: MutableList<ShipsystemData> = ArrayList()
                        var list = LoadedData.LoadedShipsystemData.get(mod.id)
                        list!!.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalList.add(data) }
                        LoadedData.LoadedShipsystemData.get(mod.id)!!.removeAll(removalList) }
                    println("Loaded ${LoadedData.LoadedShipsystemData.get(mod.id)!!.size} Shipsystems for ${mod.id} in ${timeInMillis}ms")
                }
                catch (e: Throwable)
                {
                    println("Failed to load Shipsystems for ${mod.id}")
                    println(e.printStackTrace())
                }
            }
        }
    }
}