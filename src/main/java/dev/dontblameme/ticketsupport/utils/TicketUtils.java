package dev.dontblameme.ticketsupport.utils;

import dev.dontblameme.ticketsupport.support.CustomServer;
import dev.dontblameme.ticketsupport.support.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TicketUtils {

    private TicketUtils() {}

    private static final ArrayList<CustomServer> SERVERS = new ArrayList<>();
    private static final ConfigUtils SERVERS_CONFIG = new ConfigUtils("serverConfigs.json");
    private static final ConfigUtils TICKETS_CONFIG = new ConfigUtils("serverTickets.json");

    public static void load() {
        loadServers();
    }

    private static void loadTickets(CustomServer server) {
        List<Map<?, ?>> entries = TICKETS_CONFIG.getEntries();

        if(entries == null || entries.isEmpty()) return;

        entries.forEach(entry -> entry.forEach((key, value) -> {
            ArrayList<String> arraylistValue = (ArrayList<String>) value;
            long guildId = Long.parseLong(arraylistValue.get(0));

            if(guildId != server.getGuildId()) return;

            String ticketId = (String) key;
            long guildChannelId = Long.parseLong(arraylistValue.get(1));
            long authorId = Long.parseLong(arraylistValue.get(2));
            Ticket ticket = new Ticket(authorId, getServer(guildId), ""+guildChannelId);

            ticket.setId(ticketId);

            server.getTickets().add(ticket);
        }));
    }

    private static void loadServers() {
        List<Map<?, ?>> entries = SERVERS_CONFIG.getEntries();

        if(entries == null || entries.isEmpty()) return;

        entries.forEach(entry -> entry.forEach((key, value) -> {
            ArrayList<String> arraylistValue = (ArrayList<String>) value;
            long guildId = Long.parseLong((String) key);
            long ticketChannelId = Long.parseLong(arraylistValue.get(0));
            long staffRoleId = Long.parseLong(arraylistValue.get(1));
            CustomServer server = new CustomServer(guildId, ticketChannelId, staffRoleId);

            SERVERS.add(server);
            loadTickets(server);
        }));
    }

    public static void save() {
        saveServerTickets();
        saveServerConfigs();
    }

    private static void saveServerTickets() {
        TICKETS_CONFIG.clear();
        SERVERS.forEach(server -> server.getTickets().forEach(TICKETS_CONFIG::write));
    }

    private static void saveServerConfigs() {
        SERVERS_CONFIG.clear();
        SERVERS.forEach(SERVERS_CONFIG::write);
    }

    public static void addServer(CustomServer server) {
        SERVERS.add(server);
    }

    public static void removeServer(CustomServer server) {
        SERVERS.remove(server);
    }

    public static CustomServer getServer(long id) {
        return SERVERS.stream().filter(server -> server.getGuildId() == id).findFirst().orElse(null);
    }

    public static boolean existsServer(long id) {
        return SERVERS.stream().anyMatch(server -> server.getGuildId() == id);
    }
}
