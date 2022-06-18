package dev.dontblameme.ticketsupport.main;

import dev.dontblameme.ticketsupport.commands.*;
import dev.dontblameme.ticketsupport.support.Events;
import dev.dontblameme.ticketsupport.utils.TicketUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class Main {

    private static JDA jda;
    private static final ArrayList<Command> COMMAND_LIST = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException, LoginException {

        File tokenFile = new File("token.txt");

        if(!tokenFile.exists() && !tokenFile.createNewFile())
            throw new FileNotFoundException("Could not create " + tokenFile.getName() + " file, please create it yourself.");

        if(!tokenFile.isFile())
            throw new IllegalStateException(tokenFile.getName() + " is no file.");

        String token = Files.readString(tokenFile.toPath());

        if(token.isEmpty())
            throw new IllegalStateException("Please provide a token inside of the " + tokenFile.getName() + " file.");

        jda = JDABuilder.createLight(token, EnumSet.allOf(GatewayIntent.class))
                .addEventListeners(new Events())
                .setActivity(Activity.playing("/Support to get Help"))
                .build();

        jda.awaitReady();
        TicketUtils.load();

        COMMAND_LIST.add(new Close());
        COMMAND_LIST.add(new Invite());
        COMMAND_LIST.add(new Setup());
        COMMAND_LIST.add(new Support());

        CommandListUpdateAction commands = Objects.requireNonNull(jda.updateCommands());

        for(Command c : COMMAND_LIST)
            commands.addCommands(c.getCommand());

        commands.queue();

        Runtime.getRuntime().addShutdownHook(new Thread(TicketUtils::save));
    }

    public static JDA getJDA() {
        return jda;
    }

    public static List<Command> getCommands() {
        return COMMAND_LIST;
    }

}
