package dev.dontblameme.ticketsupport.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Invite extends Command {

    public Invite() {
        super("invite", "Add the bot to your server", false, Permission.UNKNOWN);
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent e) {
        e.reply("You can add this bot to your server by using [this](https://discord.com/oauth2/authorize?client_id=974255886811942943&permissions=2415995928&scope=bot%20applications.commands) link").setEphemeral(true).queue();
    }

}
