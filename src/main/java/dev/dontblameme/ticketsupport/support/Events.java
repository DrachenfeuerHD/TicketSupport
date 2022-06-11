package dev.dontblameme.ticketsupport.support;

import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Events extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent e) {

        if(e.getGuild() == null) return;

        if(e.getName().equals("support")) {

            TextInput body = TextInput.create("body", "Question", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Your question goes here")
                    .setRequiredRange(15, 1500)
                    .build();

            Modal modal = Modal.create("support", "Support")
                    .addActionRows(ActionRow.of(body))
                    .build();

            e.replyModal(modal).queue();

        } else if(e.getName().equals("close")) {

            if(!TicketUtils.containsTicket((TextChannel) e.getChannel())) {
                e.reply("This is not a ticket").setEphemeral(true).queue();
                return;
            }

            Ticket ticket = TicketUtils.getTicket((TextChannel) e.getChannel());

            if(!ticket.getUser().getId().equals(e.getUser().getId()) && !Objects.requireNonNull(e.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
                e.reply("You are not permitted to close this ticket").setEphemeral(true).queue();
                return;
            }

            ticket.close();
            e.reply("Successfully closed this ticket").setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent e) {
        if(!e.getModalId().equals("support") || e.getValue("body") == null) return;

        String body = Objects.requireNonNull(e.getValue("body")).getAsString();

        Ticket ticket = new Ticket(body, e);

        e.reply("Your ticket has been created! You can find it at <#" + ticket.getChannel().getId() + ">").setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent e) {

        String buttonID = e.getButton().getId();

        if(e.getGuild() == null || buttonID == null || !buttonID.startsWith("close:") || buttonID.split(":")[1] == null) return;

        String ticketID = buttonID.split(":")[1];

        if(!TicketUtils.containsTicket(ticketID)) {
            e.reply("This Ticket does not exist").setEphemeral(true).complete();
            return;
        }

        Ticket ticket = TicketUtils.getTicket(ticketID);

        if(!Objects.requireNonNull(e.getMember()).hasPermission(ticket.getChannel(), Permission.MANAGE_CHANNEL)) {
            e.reply("You are not permitted to close this ticket").setEphemeral(true).queue();
            return;
        }

        ticket.close();
        e.reply("Successfully closed this ticket").setEphemeral(true).queue();
    }

}
