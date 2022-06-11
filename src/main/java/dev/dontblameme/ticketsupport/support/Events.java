package dev.dontblameme.ticketsupport.support;

import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
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

        if(e.getName().equals("setup")) {

            if(!Objects.requireNonNull(e.getMember()).hasPermission(Permission.ADMINISTRATOR)) {
                e.reply("You are not permitted to use this command").setEphemeral(true).queue();
                return;
            }

            Category category = (Category) Objects.requireNonNull(e.getOption("category")).getAsGuildChannel();
            Role role = Objects.requireNonNull(e.getOption("role")).getAsRole();

            TicketUtils.addServer(new CustomServer(e.getGuild().getIdLong(), category.getIdLong(), role.getIdLong()));

            e.reply("The server has been successfully set up").setEphemeral(true).queue();
            return;
        } else if(e.getName().equals("invite")) {
            e.reply("You can add this bot to your server by using [this](https://discord.com/oauth2/authorize?client_id=974255886811942943&scope=bot&permissions=8) and then [this](https://discord.com/api/oauth2/authorize?client_id=974255886811942943&scope=applications.commands) link").setEphemeral(true).queue();
            return;
        }

        if(!TicketUtils.existsServer(e.getGuild().getIdLong())) {
            e.reply("This server is not setup yet. Contact an administrator who can use the /setup command").setEphemeral(true).queue();
            return;
        }

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

            Ticket ticket = TicketUtils.getServer(e.getGuild().getIdLong()).getTicket(e.getChannel().getIdLong());

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

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent e) {
        if(!e.getModalId().equals("support") || e.getValue("body") == null) return;

        String body = Objects.requireNonNull(e.getValue("body")).getAsString();

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
