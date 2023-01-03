package data

import data.loading.*


class Loader()
{
    fun load(basefolder: String)
    {
        var mod = LoadModInfo().load(basefolder)

        println("\n\nLoading Data")
        println("Id: ${mod.id}")
        println("Name: ${mod.name}")
        println("Version: ${mod.version}")

        //Load Description
        try {
            LoadDescriptionData(basefolder, mod.id).load()
        }
        catch (e: Throwable)
        {
            println("Failed to load Descriptions for ${mod.id}")
            println(e.printStackTrace())
        }

        //Load Ships
        try {
            LoadShipData(basefolder, mod.id).load()
        }
        catch (e: Throwable)
        {
            println("Failed to load Ships for ${mod.id}")
            println(e.printStackTrace())
        }

        //Load Hullmods
        try {
            LoadHullmodsData(basefolder, mod.id).load()
        }
        catch (e: Throwable)
        {
            println("Failed to load Hullmods for ${mod.id}")
            println(e.printStackTrace())
        }

        //Load Systems
        try {
            LoadSystemData(basefolder, mod.id).load()
        }
        catch (e: Throwable)
        {
            println("Failed to load Shipsystems for ${mod.id}")
            println(e.printStackTrace())
        }

















    }
}