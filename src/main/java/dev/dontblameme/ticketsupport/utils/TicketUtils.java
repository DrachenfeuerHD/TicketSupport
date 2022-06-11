package dev.dontblameme.ticketsupport.utils;

import dev.dontblameme.ticketsupport.support.CustomServer;
import dev.dontblameme.ticketsupport.support.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TicketUtils {

    private TicketUtils() {}

    private static final ArrayList<CustomServer> servers = new ArrayList<>();
    private static final Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final ConfigUtils serversConfig = new ConfigUtils("serverConfigs.json");
    private static final ConfigUtils ticketsConfig = new ConfigUtils("serverTickets.json");

    public static void load() {
        loadServers();
    }

    private static void loadTickets(CustomServer server) {

        List<Map<?, ?>> entries = ticketsConfig.getEntries();

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
        List<Map<?, ?>> entries = serversConfig.getEntries();

        if(entries == null || entries.isEmpty()) return;

        entries.forEach(entry -> entry.forEach((key, value) -> {

            ArrayList<String> arraylistValue = (ArrayList<String>) value;
            long guildId = Long.parseLong((String) key);
            long ticketChannelId = Long.parseLong(arraylistValue.get(0));
            long staffRoleId = Long.parseLong(arraylistValue.get(1));
            CustomServer server = new CustomServer(guildId, ticketChannelId, staffRoleId);

            servers.add(server);
            loadTickets(server);
        }));
    }

    public static void save() {
        saveServerTickets();
        saveServerConfigs();
    }

    private static void saveServerTickets() {
        ticketsConfig.clear();
        servers.forEach(server -> server.getTickets().forEach(ticketsConfig::write));
    }

    private static void saveServerConfigs() {
        serversConfig.clear();
        servers.forEach(serversConfig::write);
    }

    public static void addServer(CustomServer server) {
        servers.add(server);
    }

    public static CustomServer getServer(long id) {
        return servers.stream().filter(server -> server.getGuildId() == id).findFirst().orElse(null);
    }

    public static boolean existsServer(long id) {
        return servers.stream().anyMatch(server -> server.getGuildId() == id);
    }

    public static boolean isNumeric(String strNum) {
        if(strNum == null) return false;

        return numberPattern.matcher(strNum).matches();
    }
}
