package dev.dontblameme.ticketsupport.commands;

import dev.dontblameme.ticketsupport.support.Ticket;
import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class Close extends Command {

    public Close() {
        super("close", "Close a ticket", true, Permission.UNKNOWN);
    }

    @Override
    protected void onCommand(SlashCommandInteractionEvent e) {

        Ticket ticket = TicketUtils.getServer(Objects.requireNonNull(e.getGuild()).getIdLong()).getTicket(e.getChannel().getIdLong());

        if(ticket == null || ticket.getChannel() == null || !TicketUtils.getServer(e.getGuild().getIdLong()).containsTicket(e.getChannel().getIdLong())) {
            e.reply("This is not a valid ticket").setEphemeral(true).queue();
            return;
        }


        if(!ticket.getUser().getId().equals(e.getUser().getId()) && !Objects.requireNonNull(e.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
            e.reply("You are not permitted to close this ticket").setEphemeral(true).queue();
            return;
        }

        ticket.close();
        e.reply("Successfully closed this ticket").setEphemeral(true).queue();
    }

}
