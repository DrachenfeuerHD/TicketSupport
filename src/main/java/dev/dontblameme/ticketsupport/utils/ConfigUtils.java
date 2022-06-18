package dev.dontblameme.ticketsupport.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.dontblameme.ticketsupport.support.CustomServer;
import dev.dontblameme.ticketsupport.support.Ticket;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ConfigUtils {

    private final File file;
    private final Gson gson;

    public ConfigUtils(String fileName) {
        gson = new Gson();
        file = new File(fileName);

        try {
            if(!getFile().exists() && !getFile().createNewFile())
                throw new FileNotFoundException("Could not create " + fileName + " file, please create it yourself.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Map<?, ?>> getEntries() {
        try(BufferedReader reader = Files.newBufferedReader(getFile().toPath())) {

            List<Map<?, ?>> list = new ArrayList<>();

            reader.lines().forEach(line -> {
                Map<?, ?> map = gson.fromJson(line, Map.class);

                list.add(map);
            });

            return list.isEmpty() ? Collections.emptyList() : list;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public void write(Ticket ticket) {
        try {
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();

            array.add(""+ticket.getServer().getGuildId());
            array.add(""+ticket.getChannel().getId());
            array.add(""+ticket.getUser().getId());

            json.add(ticket.getId()+"", array);

            Files.write(getFile().toPath(), (json + "\n").getBytes(), StandardOpenOption.APPEND);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void write(CustomServer customServer) {
        try {
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();

            array.add(""+customServer.getTicketsChannel());
            array.add(""+customServer.getStaffRoleId());

            json.add(customServer.getGuildId()+"", array);

            Files.write(getFile().toPath(), (json + "\n").getBytes(), StandardOpenOption.APPEND);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        try(Writer writer = Files.newBufferedWriter(getFile().toPath())) {

            writer.write("");

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }
}
