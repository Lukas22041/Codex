package data.loading

import data.*

class Cleanup
{
    fun gather()
    {
        var removalListShips: MutableList<ShipData> = ArrayList()
        var removalListWeapons: MutableList<WeaponData> = ArrayList()
        var removalListHullmods: MutableList<HullmodData> = ArrayList()
        var removalListSystems: MutableList<ShipsystemData> = ArrayList()

        LoadedData.LoadedShipData.forEach { mod ->
            mod.value.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalListShips.add(data) } }

        LoadedData.LoadedWeaponData.forEach { mod ->
            mod.value.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalListWeapons.add(data) } }

        LoadedData.LoadedHullmodData.forEach { mod ->
            mod.value.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalListHullmods.add(data) } }

        LoadedData.LoadedShipsystemData.forEach { mod ->
            mod.value.forEach { data -> if (data.id == "" || data.name.contains("#") || data.name == "") removalListSystems.add(data) } }


        LoadedData.LoadedShipData.forEach { mod ->
            mod.value.removeAll(removalListShips) }

        LoadedData.LoadedWeaponData.forEach { mod ->
            mod.value.removeAll(removalListWeapons) }

        LoadedData.LoadedHullmodData.forEach { mod ->
            mod.value.removeAll(removalListHullmods) }

        LoadedData.LoadedShipsystemData.forEach { mod ->
            mod.value.removeAll(removalListSystems) }

    }

}