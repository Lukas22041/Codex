package bot.commands

import bot.util.BaseCommand
import bot.util.CommandUtil
import bot.util.CommandUtil.getFuzzyWeapon
import bot.util.CommandUtil.trimAfter
import data.LoadedData
import dev.kord.common.Color
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.embed
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class ShowWeapon : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {

        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true; }
            string("weapon", "name or id of the weapon") { required = true; }
            boolean("private", "Causes the message to only show for you.")
        }
    }

    override suspend fun onCommandUse(interaction: ChatInputCommandInteraction)
    {
        //Look for Mod and Weapon data
        val command = interaction.command
        val modInput = command.strings["source"]!!
        val weaponInput = command.strings["weapon"]!!
        var private = false
        try {
            private = command.optional().value.booleans["private"]!!
        }
        catch (e: Throwable) {}

        var modData = CommandUtil.loadModData(modInput, interaction) ?: return
        if (LoadedData.LoadedWeaponData.get(modData.id).isNullOrEmpty())
        {
            interaction.deferEphemeralResponse().respond { content = "Requested mod \"${modData.name}\" has no weapons." }
            return
        }
        var weaponData = LoadedData.LoadedWeaponData.get(modData.id)!!.find { it.id.lowercase() == weaponInput.lowercase() || it.name.lowercase() == weaponInput.lowercase() }
        if (weaponData == null) weaponData = getFuzzyWeapon(modData.id, weaponInput)
        if (weaponData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Unable to find weapon going by \"$weaponInput\" in ${modData.name}" }
            return
        }

        //Setup general data required for the card
        var weaponDescriptionsData = LoadedData.LoadedDescriptionData.get(modData.id)!!.find { it.id == weaponData.id }

        var chargedown = 0f
        var chargeup = 0f
        var burstdelay = 0f
        var firerate = 0f
        var burstSize = 0f
        try {
            if (weaponData.chargedown != "") chargedown = weaponData.chargedown.toFloat()
            if (weaponData.chargeup != "") chargeup = weaponData.chargeup.toFloat()
            if (weaponData.burstDelay != "") burstdelay = weaponData.burstDelay.toFloat()
            if (weaponData.burstSize != "") burstSize = weaponData.burstSize.toFloat()
            firerate = (chargedown + chargeup + burstdelay)
            if (weaponData.energyPerSecond != "") firerate += burstSize
        }
        catch (e: Throwable) {}

        var reloadRate = 0f
        var ammoPerRegen = 1f
        var restock = 0f

        try {
            if (weaponData.ammoPerSecond != "") reloadRate = weaponData.ammoPerSecond.toFloat()
            if (weaponData.reloadSize != "") ammoPerRegen = weaponData.reloadSize.toFloat()
            restock = ammoPerRegen / reloadRate
        }
        catch (e: Throwable) {}

        var size = ""
        if (weaponData.burstSize != "")
        {
            size = "x${weaponData.burstSize}"
        }

        var generalData = ""
        generalData += "Name: ``${weaponData.name}``\n"
        generalData += "ID: ``${weaponData.id}``\n"

        var symbol = DecimalFormatSymbols(Locale.getDefault())
        var format = DecimalFormat("######.##", symbol)

        if (weaponData.size != "") generalData += "\nSize: ``${weaponData.size.lowercase().capitalize()}``\n"
        if (weaponData.type != "") generalData += "Type: ``${weaponData.type.lowercase().capitalize()}``\n"
        if (weaponData.damageType != "") generalData += "Damage Type: ``${weaponData.damageType.lowercase().capitalize()}``\n"

        if (weaponData.ordnancePoints != "") generalData += "Ordnance Points: ``${weaponData.ordnancePoints}``\n"
        if (weaponData.primaryRoleStr != "") generalData += "\nRole: ``${weaponData.primaryRoleStr}``\n"

        var stats = ""
        if (firerate != 0f) stats += "Rate of Fire: ``Every ${format.format(firerate)}s``\n"
        if (weaponData.range != "") stats += "Range: ``${weaponData.range}``\n"

        if (weaponData.damagePerSecond != "") stats += "Damage/Second: ``${format.format((weaponData.damagePerSecond.toFloat() * (chargeup + burstSize)) / firerate)}``\n"
        if (weaponData.damagePerShot != "") stats += "Damage/Shot: ``${weaponData.damagePerShot}$size``\n"

        if (weaponData.energyPerSecond != "" && weaponData.damagePerSecond != "" ) stats += "Flux/Damage: ``${format.format(weaponData.energyPerSecond.toFloat() / weaponData.damagePerSecond.toFloat())}``\n"
        if (weaponData.energyPerShot != "" && weaponData.damagePerShot != "" && weaponData.energyPerShot != "0") stats += "Flux/Damage: ``${format.format(weaponData.energyPerShot.toFloat() / weaponData.damagePerShot.toFloat())}``\n"

        if (weaponData.energyPerSecond != "") stats += "Flux/Second: ``${format.format((weaponData.energyPerSecond.toFloat() * (chargeup + burstSize )) / firerate)}``\n"

        if (weaponData.energyPerShot != "" && weaponData.energyPerShot != "0") stats += "Flux/Shot: ``${format.format(weaponData.energyPerShot.toFloat())}``\n"
        if (weaponData.emp != "") stats += "EMP: ``${weaponData.emp}``\n"
        if (weaponData.ammo != "") stats += "Ammo: ``${weaponData.ammo}``\n"
        if (restock != 0f && !restock.isInfinite() && !restock.isNaN()) stats += "Reload Time: ``Every ${format.format(restock.toFloat())}/s``\n"
        if (restock != 0f && !restock.isInfinite() && !restock.isNaN()) stats += "Ammo per Reload: ``${format.format(ammoPerRegen.toFloat())}``\n"

        //Do the response to the command
        val response = when(private)
        {
            true -> interaction.deferEphemeralResponse()
            false -> interaction.deferPublicResponse()
            else -> interaction.deferPublicResponse()
        }
        response.respond {

            embed {
                title = "Weapon: ${weaponData.name}"
                if (weaponDescriptionsData != null) {
                    description = weaponDescriptionsData.text1.trimAfter(600)
                }
                else
                {
                    description = ""
                }

                if (generalData != "")
                field {
                    name = "General Data\n"
                    value = generalData
                    inline = true
                }

                if (stats != "")
                field {
                    name = "Stats"
                    value = stats
                    inline = true
                }

                if (weaponData.customPrimary != "")
                {
                    field {
                        name = "Weapon Effect"

                        var stringStats: List<String>
                        var finalString = weaponData.customPrimary
                        if (weaponData.customPrimaryHL != "")
                        {
                            try {
                                stringStats = weaponData.customPrimaryHL.split('|')
                                for (stat in stringStats)
                                {
                                    finalString = finalString.replaceFirst("%s", stat)
                                }
                            }
                            catch (e: Throwable) {}
                        }

                        value = finalString
                    }
                }

                footer {
                    this.text = "Loaded from ${modData.name} (V${modData.version})"
                }

                this.color = Color(10, 50, 155)
            }

            if (!private)
            {
                CommandUtil.addDeleteButton(this, interaction)
            }
        }
    }
}