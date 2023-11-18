package bot

import bot.commands.*
import data.LoadedData
import data.StarmodderData
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL
import java.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime


var token = File("bot-token.txt").readText().trim()

class BotMain
{
    @OptIn(ExperimentalTime::class)
    suspend fun init()
    {




        var kord = Kord(token) {

        }

        //Load Commands
        CodexInfo().init(kord, "codex", "Show Codex Bot Information")
        StarmodderCommand().init(kord, "mod", "Searches through \"starmodder.pages.dev\" to look up data on a starsector mod")
        ShowShip().init(kord, "ship", "Displays ship data (Does not currently show skins or variants)")
        ShowWeapon().init(kord, "weapon", "Displays weapon data")
        ShowHullmod().init(kord, "hullmod", "Displays weapon data")
        ShowShipsystem().init(kord, "system", "Displays shipsystem data")

        kord.on<GuildButtonInteractionCreateEvent> {
            var response = interaction.deferEphemeralResponse()

            var buttonData: ButtonData = Json.decodeFromString(ButtonData.serializer(), interaction.componentId)

            if (buttonData.buttonID == "delete_post")
            {
                response.respond {
                    if (buttonData.user == interaction.user.memberData.userId.value)
                    {
                        content = "Removed Message"
                        interaction.message.delete("Deleted by user request")
                    }
                    else
                    {
                        content = "Lacking permissions to delete the message. You can only delete messages that you yourself requested."
                    }
                }
            }
        }

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
            println("\nBot Started")
        }
    }

    suspend fun loadStarmodderData() {
        while(true) {

            var time = measureTimeMillis {
                try {
                    LoadedData.StarmodderData.clear()
                    var stream = URL("https://raw.githubusercontent.com/wispborne/StarsectorModRepo/main/ModRepo.json").openStream()
                    var scanner = Scanner(stream, "UTF-8").useDelimiter("\\A")
                    var starmodderJson = scanner.next()

                    var json = JSONObject(starmodderJson)

                    var items = json.get("items") as JSONArray
                    for (entry in items.iterator()) {
                        if (entry !is JSONObject) continue

                        var name = entry.get("name") as String
                        var version = entry.opt("gameVersionReq") as String?
                        var summary = entry.opt("summary") as String?
                        var author = entry.opt("authors") as String?

                        var urls = entry.get("urls")
                        var downloadURL: String? = ""
                        var forumURL: String? = ""
                        var discordURL: String? = ""
                        if (urls is JSONObject) {
                            downloadURL = urls.opt("DownloadPage") as String?
                            forumURL = urls.opt("Forum") as String?
                            discordURL = urls.opt("Discord") as String?
                        }

                        var imageUrl: String? = ""
                        var images = entry.opt("images")
                        if (images is JSONObject) {
                            for (id in images.keys()) {
                                var image = images.get(id) ?: continue
                                if (image !is JSONObject) continue
                                imageUrl = image.opt("url") as String?
                                break
                            }
                        }

                        var data = StarmodderData(name, summary,  author, version, downloadURL, forumURL, discordURL, imageUrl)
                        LoadedData.StarmodderData.add(data)
                    }
                } catch (e: Throwable) {
                    println("Error after reloading Starmodder Json")
                }
            }



            println("Reloaded Starmodder Json in ${time}ms")


            delay(12.hours)
        }
    }
}


@Serializable
data class ButtonData(val user: ULong, var buttonID: String)