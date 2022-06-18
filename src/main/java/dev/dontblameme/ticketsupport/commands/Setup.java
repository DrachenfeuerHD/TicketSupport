package dev.dontblameme.ticketsupport.commands;

import dev.dontblameme.ticketsupport.support.CustomServer;
import dev.dontblameme.ticketsupport.support.Ticket;
import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Setup extends Command {

    public Setup() {
        super("setup", "Setup a server", false, Permission.ADMINISTRATOR, new OptionData(OptionType.CHANNEL, "category", "Category for tickets", true).setChannelTypes(ChannelType.CATEGORY), new OptionData(OptionType.ROLE, "role", "Role for managing tickets", true));
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent e) {

        Category category = (Category) Objects.requireNonNull(e.getOption("category")).getAsGuildChannel();
        Role role = Objects.requireNonNull(e.getOption("role")).getAsRole();

        if(TicketUtils.existsServer(Objects.requireNonNull(e.getGuild()).getIdLong())) {

            List<Ticket> ticketList = new ArrayList<>(TicketUtils.getServer(e.getGuild().getIdLong()).getTickets());

            TicketUtils.removeServer(TicketUtils.getServer(e.getGuild().getIdLong()));
            TicketUtils.addServer(new CustomServer(e.getGuild().getIdLong(), category.getIdLong(), role.getIdLong()));

            ticketList.forEach(ticket -> TicketUtils.getServer(e.getGuild().getIdLong()).addTicket(ticket));

            e.reply("The server has been updated successfully").setEphemeral(true).queue();
            return;
        }

        TicketUtils.addServer(new CustomServer(e.getGuild().getIdLong(), category.getIdLong(), role.getIdLong()));

        e.reply("The server has been successfully set up").setEphemeral(true).queue();
    }

}
