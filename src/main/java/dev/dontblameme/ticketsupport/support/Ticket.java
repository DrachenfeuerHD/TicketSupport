package dev.dontblameme.ticketsupport.support;

import dev.dontblameme.ticketsupport.main.Main;
import dev.dontblameme.ticketsupport.utils.TicketUtils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class Ticket {
    private final long userID;
    @Getter
    private final User user;
    @Getter
    private final String guildID;
    @Getter
    private TextChannel channel;

    public Ticket(long userID, String guildID, String channelID) {
        this.userID = userID;
        this.guildID = guildID;

        List<Member> members = Objects.requireNonNull(Main.getJDA().getGuildById(guildID)).loadMembers().get();

        this.user = Objects.requireNonNull(members.stream().filter(m -> m.getIdLong() == userID).findFirst().orElse(null)).getUser();
        this.channel = Objects.requireNonNull(Main.getJDA().getGuildById(guildID)).getTextChannelById(channelID);
    }

    public Ticket(String body, ModalInteractionEvent event) {

        this.userID = event.getUser().getIdLong();
        this.guildID = Objects.requireNonNull(event.getGuild()).getId();
        this.user = event.getUser();

        create(body);
    }

    private void create(String body) {

        this.channel = TicketUtils.getCategory().createTextChannel(user.getName())
                .addMemberPermissionOverride(user.getIdLong(), EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_ADD_REACTION))
                .addRolePermissionOverride(TicketUtils.getRole().getIdLong(), EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), EnumSet.of(Permission.UNKNOWN))
                .addRolePermissionOverride(TicketUtils.getGuild().getPublicRole().getIdLong(), EnumSet.of(Permission.UNKNOWN), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();

        MessageEmbed msg = new EmbedBuilder()
                .setTitle("Support ticket by " + user.getName())
                .addField("Information", "User: " + user.getAsMention() + "\nID: " + userID, true)
                .setDescription(body)
                .setFooter(user.getName(), (user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl()))
                .build();

        channel.sendMessageEmbeds(msg).setActionRow(Button.danger("close:" + channel.getId(), "Close")).complete();

        channel.sendMessage(TicketUtils.getRole().getAsMention()).complete();

        TicketUtils.addTicket(this);
    }

    public void close() {

        channel.delete().complete();

        TicketUtils.removeTicket(this);

        MessageEmbed builder = new EmbedBuilder()
                .setTitle("Closed " + user.getName())
                .addField("Information", "User: " + user.getAsMention() + "\nID: " + userID, true)
                .setDescription("Your ticket has been closed by a staff member")
                .setFooter(user.getName(), (user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl()))
                .build();

        user.openPrivateChannel().submit()
                .thenCompose(privateChannel -> privateChannel.sendMessageEmbeds(builder).submit())
                .whenComplete((message, error) -> {});
    }

    public String toString() {
        return userID + ":" + channel.getGuild().getId() + ":" + channel.getId();
    }

}
