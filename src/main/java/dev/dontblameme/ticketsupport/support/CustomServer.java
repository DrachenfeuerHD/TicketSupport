package dev.dontblameme.ticketsupport.support;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class CustomServer {

    private final long guildId;
    private final long staffRoleId;
    private final long ticketsChannel;
    private final ArrayList<Ticket> tickets = new ArrayList<>();

    public CustomServer(long guildId, long ticketsChannel, long staffRoleId) {
        this.guildId = guildId;
        this.ticketsChannel = ticketsChannel;
        this.staffRoleId = staffRoleId;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    public boolean containsTicket(long channelId) {
        return tickets.stream().anyMatch(t -> t.getChannel().getIdLong() == channelId);
    }

    public Ticket getTicket(long channelId) {
        return tickets.stream().filter(t -> t.getChannel().getIdLong() == channelId).findAny().orElse(null);
    }
}
