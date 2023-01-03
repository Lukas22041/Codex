package bot.buttons

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.on

class InteractionButtons
{
    fun init(kord: Kord)
    {
        kord.on<GuildButtonInteractionCreateEvent> {

        }
    }
}