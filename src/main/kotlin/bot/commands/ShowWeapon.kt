package bot.commands

import bot.ButtonData
import bot.util.BaseCommand
import bot.util.CommandUtil.getFuzzyMod
import bot.util.CommandUtil.getFuzzyShip
import bot.util.CommandUtil.getFuzzyWeapon
import bot.util.CommandUtil.trimAfter
import data.LoadedData
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.optional.optional
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.serialization.json.Json

class ShowWeapon : BaseCommand()
{
    override suspend fun registerCommand(kord: Kord, commandID: String, commandDesc: String)
    {
        kord.createGlobalChatInputCommand(commandID, commandDesc) {
            string("source", "ID or Name of where a ship is from (i.e Starsector)") { required = true}
            string("weapon", "name or id of the weapon") { required = true}
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

        var modData = LoadedData.LoadedModData.find { it.id.lowercase() == modInput.lowercase() || it.name.lowercase() == modInput.lowercase() }
        if (modData == null) modData = getFuzzyMod(modInput)
        if (modData == null)
        {
            interaction.deferEphemeralResponse().respond { content = "Unable to find mod \"$modInput\" in the bots database. Use /codex to look for available mods." }
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

        var generalData = ""
        generalData += "Name: ``${weaponData.name}``\n"
        generalData += "ID: ``${weaponData.id}``\n"

        if (weaponData.size != "") generalData += "\nSize: ``${weaponData.size.lowercase().capitalize()}``\n"
        if (weaponData.type != "") generalData += "Type: ``${weaponData.type.lowercase().capitalize()}``\n"
        if (weaponData.damageType != "") generalData += "Damage Type: ``${weaponData.damageType.lowercase().capitalize()}``\n"

        if (weaponData.ordnancePoints != "") generalData += "Ordnance Points: ``${weaponData.ordnancePoints}``\n"
        if (weaponData.primaryRoleStr != "") generalData += "\nRole: ``${weaponData.primaryRoleStr}``\n"

        var stats = ""
        if (weaponData.range != "") stats += "Range: ``${weaponData.range}``\n"
        if (weaponData.damagePerSecond != "") stats += "Damage/Second: ``${weaponData.damagePerSecond}``\n"
        if (weaponData.damagePerShot != "") stats += "Damage/Shot: ``${weaponData.damagePerShot}``\n"
        if (weaponData.energyPerSecond != "") stats += "Energy/Second: ``${weaponData.energyPerSecond}``\n"
        if (weaponData.energyPerShot != "" && weaponData.energyPerShot != "0") stats += "Energy/Shot: ``${weaponData.energyPerShot}``\n"
        if (weaponData.emp != "") stats += "EMP: ``${weaponData.emp}``\n"
        if (weaponData.ammo != "") stats += "Ammo: ``${weaponData.ammo}``\n"
        if (weaponData.ammoPerSecond != "") stats += "Ammo/Second: ``${weaponData.ammoPerSecond}``\n"


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

                footer {
                    this.text = "Loaded from ${modData.name} (V${modData.version})"
                }

                this.color = Color(10, 50, 155)
            }

            if (!private)
            {
                val emote = ReactionEmoji.Unicode("❌")
                actionRow {
                    var userdata = ButtonData(interaction.user.data.id.value, "delete_post")
                    this.interactionButton(ButtonStyle.Primary,  Json.encodeToString(ButtonData.serializer(), userdata)) {
                        this.label = "Delete"

                        emoji(emote)
                    }
                }
            }
        }
    }
}