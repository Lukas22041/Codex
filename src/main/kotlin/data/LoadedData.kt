package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModData(var id: String, var name: String, var version: String)

@Serializable
data class ShipData(var name: String = "", var id: String = "", var designation: String = "", var systemID: String, var tech: String = "",
                    var hullSize: String = "", var ordnancePoints: String = "", var deploymentPoints: String = "", var builtInMods: List<String>? = null, var weaponSlots: Map<String, Int> = HashMap(),
                    var hitpoints: String = "", var armorRating: String = "", var maxFlux: String = "", var fluxDissipation: String = "",
                    var fighterBays: String = "", var maxSpeed: String = "", var shieldType: String = "", var shieldArc: String = "", var shieldEfficiency: String = "")

@Serializable
data class WeaponData(var name: String, var id: String, val range: String, @SerialName("damage/second") var damagePerSecond: String, @SerialName("damage/shot") var damagePerShot: String,
                      var emp: String, @SerialName("turn rate") var turnRate: String, @SerialName("OPs") var ordnancePoints: String, @SerialName("energy/shot") var energyPerShot: String,
                      @SerialName("energy/second") var energyPerSecond: String, var ammo: String,  @SerialName("ammo/sec") var ammoPerSecond: String, @SerialName("type") var damageType: String, var primaryRoleStr: String)

@Serializable
data class DescriptionsData(var id: String,var type: String, var text1: String, var text2: String, var text3: String, var text4: String)

@Serializable
data class HullmodData(var name: String, var id: String, var tags: String, var uiTags: String,
                       var cost_frigate: String, var cost_dest: String, var cost_cruiser: String, var cost_capital: String)

@Serializable
data class ShipsystemData(var name: String, var id: String, val active: String, val cooldown: String, val toggle: String)

object LoadedData
{
    var LoadedModData: MutableList<ModData> = ArrayList()

    var LoadedDescriptionData: MutableMap<String, MutableList<DescriptionsData>> = HashMap()

    var LoadedShipData: MutableMap<String, MutableList<ShipData>> = HashMap()
    var LoadedWeaponData: MutableMap<String, MutableList<WeaponData>> = HashMap()

    var LoadedHullmodData: MutableMap<String, MutableList<HullmodData>> = HashMap()
    var LoadedShipsystemData: MutableMap<String, MutableList<ShipsystemData>> = HashMap()

}