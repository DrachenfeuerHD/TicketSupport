package dev.dontblameme.ticketsupport.utils;

import dev.dontblameme.ticketsupport.main.Main;
import dev.dontblameme.ticketsupport.support.Ticket;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TicketUtils {

    private TicketUtils() {}

    private static final ArrayList<Ticket> tickets = new ArrayList<>();
    private static String guildID;
    private static String roleID;
    private static String categoryID;
    private static final File configFile = new File("config.txt");
    private static final File ticketsFile = new File("tickets.txt");

    private static void loadConfig() throws IOException {

        if(!configFile.exists())
            createConfigFile();

        try(BufferedReader reader = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {

            reader.lines().forEach(line -> {

                String value = line.split(":")[1].replace(" ", "");
                String lineName = line.split(":")[0];

                if(!isNumeric(value))
                    throw new IllegalStateException("The config value of " + value + " may only be a number. You need to provide IDs.");

                if(lineName.equalsIgnoreCase("ServerToUse")) guildID = value;
                if(lineName.equalsIgnoreCase("CategoryForTickets")) categoryID = value;
                if(lineName.equalsIgnoreCase("RoleForManagingTickets")) roleID = value;
            });

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void createConfigFile() throws IOException {
        if(configFile.createNewFile()) {

            try(FileWriter fw = new FileWriter(configFile)) {

                fw.write("""
                            ServerToUse:Please insert the server id of the server where this bot should work
                            CategoryForTickets:Please insert the id of the category where the tickets should be added
                            RoleForManagingTickets:Please insert the id of the role which will be added to every ticket and receive the manage channel permissions
                            """);

                fw.flush();

                Logger.getAnonymousLogger().log(Level.WARNING, "{0} created, please configure it to your needs", ticketsFile.getName());
                System.exit(-1);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("Could not create " + ticketsFile.getName() + " file, please create it yourself.");
        }
    }

    private static void loadTickets() throws IOException {

        if(!ticketsFile.exists() && !ticketsFile.createNewFile())
            throw new FileNotFoundException("Could not create " + ticketsFile.getName() + " file, please create it yourself.");

        try(BufferedReader reader = Files.newBufferedReader(ticketsFile.toPath(), StandardCharsets.UTF_8)) {

            reader.lines().forEach(line -> {
                if(!line.isEmpty()) {
                    String[] lineSplit = line.split(":");
                    tickets.add(new Ticket(Long.parseLong(lineSplit[0]), lineSplit[1], lineSplit[2]));
                }
            });

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static Guild getGuild() {
        return Main.getJDA().getGuildById(guildID);
    }

    public static Role getRole() {
        return getGuild().getRoleById(roleID);
    }

    public static Category getCategory() {
        return getGuild().getCategoryById(categoryID);
    }

    public static void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public static void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    public static boolean containsTicket(TextChannel channel) {
        return tickets.stream().anyMatch(t -> t.getChannel().getId().equals(channel.getId()));
    }

    public static boolean containsTicket(String channelID) {
        return tickets.stream().anyMatch(t -> t.getChannel().getId().equals(channelID));
    }

    public static Ticket getTicket(TextChannel channel) {
        return tickets.stream().filter(t -> t.getChannel().getId().equals(channel.getId())).findFirst().orElse(null);
    }

    public static Ticket getTicket(String channelID) {
        return tickets.stream().filter(t -> t.getChannel().getId().equals(channelID)).findFirst().orElse(null);
    }

    public static void save() {

        try (BufferedWriter writer = Files.newBufferedWriter(ticketsFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write("");
        } catch(IOException e) {e.printStackTrace();}

        tickets.forEach(ticket -> {
            try (BufferedWriter writer = Files.newBufferedWriter(ticketsFile.toPath(), StandardCharsets.UTF_8)) {

                writer.append("\n").append(String.valueOf(ticket));

            } catch (IOException e) {e.printStackTrace();}
        });
    }

    public static void load() throws IOException {
        loadConfig();
        loadTickets();
    }

    private static final Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if(strNum == null) return false;

        return numberPattern.matcher(strNum).matches();
    }
}
