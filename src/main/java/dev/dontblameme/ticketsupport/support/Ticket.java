package dev.dontblameme.ticketsupport.support;

import dev.dontblameme.ticketsupport.main.Main;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Getter
public class Ticket {
    private final long userID;
    @Setter
    private User user;
    private final CustomServer server;
    private TextChannel channel;
    @Setter
    private String id;

    public Ticket(long userID, CustomServer server, String channelID) {
        this.userID = userID;
        this.server = server;
        this.id = "ticket-" + new Random().nextLong();

        Objects.requireNonNull(Main.getJDA().getGuildById(server.getGuildId())).loadMembers().onSuccess(list -> setUser(Objects.requireNonNull(list.stream().filter(m -> m.getIdLong() == userID).findFirst().orElse(null)).getUser()));

        this.channel = Objects.requireNonNull(Main.getJDA().getGuildById(server.getGuildId())).getTextChannelById(channelID);
    }

    public Ticket(long userID, CustomServer server, String body, Guild guild) {
        this.userID = userID;
        this.server = server;
        this.id = "ticket-" + new Random().nextLong();

        List<Member> members = guild.loadMembers().get();

        this.user = Objects.requireNonNull(members.stream().filter(m -> m.getIdLong() == userID).findFirst().orElse(null)).getUser();

        create(body, guild);
    }

    private void create(String body, Guild guild) {

        if(this.channel != null) return;

        this.channel = Objects.requireNonNull(guild.getCategoryById(server.getTicketsChannel())).createTextChannel(id)
                .addMemberPermissionOverride(userID, EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_ADD_REACTION))
                .addRolePermissionOverride(server.getStaffRoleId(), EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), EnumSet.of(Permission.UNKNOWN))
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), EnumSet.of(Permission.UNKNOWN), EnumSet.of(Permission.VIEW_CHANNEL))
                .complete();

        MessageEmbed msg = new EmbedBuilder()
                .setTitle("Support ticket by " + user.getName())
                .addField("Information", "User: " + user.getAsMention() + "\nID: " + userID, true)
                .setDescription(body)
                .setFooter(user.getName(), (user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl()))
                .build();

        channel.sendMessageEmbeds(msg).setActionRow(Button.danger("close:" + channel.getIdLong(), "Close")).complete();

        channel.sendMessage(Objects.requireNonNull(guild.getRoleById(server.getStaffRoleId())).getAsMention()).complete();

        server.addTicket(this);
    }

    public void close() {

        channel.delete().complete();
        server.removeTicket(this);

        Guild guild = Objects.requireNonNull(Main.getJDA().getGuildById(server.getGuildId()));

        MessageEmbed builder = new EmbedBuilder()
                .setTitle("Closed " + user.getName())
                .setThumbnail(guild.getIconUrl())
                .setDescription("Your ticket on the server " + guild.getName() + " has been closed by a staff member")
                .setFooter(user.getName(), (user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl()))
                .build();

        user.openPrivateChannel().submit()
                .thenCompose(privateChannel -> privateChannel.sendMessageEmbeds(builder).submit())
                .whenComplete((message, error) -> {});
    }

    @Override
    public String toString() {
        return userID + ":" + channel.getGuild().getId() + ":" + channel.getId();
    }

}
