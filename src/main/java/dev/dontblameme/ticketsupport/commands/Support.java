package dev.dontblameme.ticketsupport.commands;

import dev.dontblameme.ticketsupport.support.Ticket;
import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Support extends Command {

    public Support() {
        super("support", "Ask for Support", true, Permission.UNKNOWN);
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent e) {
        TextInput body = TextInput.create("body", "Question", TextInputStyle.PARAGRAPH).setPlaceholder("Your question goes here").setRequiredRange(15, 1500).build();
        Modal modal = Modal.create(getCommand().getName(), "Support").addActionRows(ActionRow.of(body)).build();

        e.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        if(!e.getModalId().equals(getCommand().getName()) || e.getValue("body") == null) return;

        String body = Objects.requireNonNull(e.getValue("body")).getAsString();

        // Here I need to use a thread because i can't load the members in the main thread. Found at Ticket.class in line 44
        new Thread(() -> {
            Ticket ticket = new Ticket(e.getUser().getIdLong(), TicketUtils.getServer(Objects.requireNonNull(e.getGuild()).getIdLong()), body, e.getGuild());

            e.reply("Your ticket has been created successfully! You can find it at <#" + ticket.getChannel().getId() + ">").setEphemeral(true).queue();
        }).start();
    }
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent e) {
        String buttonID = e.getButton().getId();

        if(e.getGuild() == null || buttonID == null || !buttonID.equals("close:" + e.getChannel().getIdLong())) return;

        Ticket ticket = TicketUtils.getServer(e.getGuild().getIdLong()).getTicket(e.getChannel().getIdLong());

        if(ticket == null || ticket.getChannel() == null || !TicketUtils.getServer(e.getGuild().getIdLong()).containsTicket(e.getChannel().getIdLong())) {
            e.reply("This is not a valid ticket").setEphemeral(true).queue();
            return;
        }

        if(!Objects.requireNonNull(e.getMember()).hasPermission(ticket.getChannel(), Permission.MANAGE_CHANNEL)) {
            e.reply("You are not permitted to close this ticket").setEphemeral(true).queue();
            return;
        }

        ticket.close();
        e.reply("Successfully closed this ticket").setEphemeral(true).queue();
    }

}
