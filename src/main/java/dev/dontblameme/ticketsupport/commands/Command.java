package dev.dontblameme.ticketsupport.commands;

import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Command {

    private final String name;
    private final String description;
    private final OptionData[] options;
    private final Permission permissionRequired;
    private final boolean setupCheck;

    public Command(String name, String description, boolean setupCheck, Permission permissionRequired, OptionData... options) {
        this.name = name;
        this.description = description;
        this.setupCheck = setupCheck;
        this.permissionRequired = permissionRequired;
        this.options = options;
    }

    public void onEvent(SlashCommandInteractionEvent e) {

        if(!Objects.requireNonNull(e.getMember()).hasPermission(permissionRequired)) {
            e.reply("You are missing the permission `" + permissionRequired.name() + "` to use this command").setEphemeral(true).queue();
            return;
        }

        if(setupCheck && (!TicketUtils.existsServer(Objects.requireNonNull(e.getGuild()).getIdLong()) || (TicketUtils.existsServer(e.getGuild().getIdLong()) && (e.getGuild().getCategoryById(TicketUtils.getServer(e.getGuild().getIdLong()).getTicketsChannel()) == null || e.getGuild().getRoleById(TicketUtils.getServer(e.getGuild().getIdLong()).getStaffRoleId()) == null)))) {
            e.reply("This server is not setup yet. Contact an administrator who can use the /setup command").setEphemeral(true).queue();
            return;
        }

        onCommand(e);
    }

    protected void onCommand(SlashCommandInteractionEvent e) {/* managed using extends Command */}

    public void onModalInteraction(ModalInteractionEvent e) {/* managed using extends Command */}

    public void onButtonInteraction(@Nonnull ButtonInteractionEvent e) {/* managed using extends Command */}

    public CommandData getCommand() {
        return options.length == 0 ? Commands.slash(name, description) : Commands.slash(name, description).addOptions(options);
    }

}
