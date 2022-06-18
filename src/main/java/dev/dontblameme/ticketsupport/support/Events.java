package dev.dontblameme.ticketsupport.support;

import dev.dontblameme.ticketsupport.main.Main;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class Events extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent e) {
        if(e.getGuild() == null) return;

        Main.getCommands().stream().filter(cmd -> e.getName().equals(cmd.getCommand().getName())).findFirst().ifPresent(c -> c.onEvent(e));
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent e) {
        Main.getCommands().forEach(c -> c.onModalInteraction(e));
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent e) {
        Main.getCommands().forEach(c -> c.onButtonInteraction(e));
    }

}
