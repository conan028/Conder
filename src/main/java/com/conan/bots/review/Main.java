package com.conan.bots.review;

import com.conan.bots.review.datastore.MySQLHandler;
import com.conan.bots.review.listeners.ReviewListener;
import com.conan.bots.review.listeners.commands.CommandManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.logging.Logger;

import static com.conan.bots.review.datastore.DatabaseHandler.dbHandler;

public class Main {

    private static final Dotenv CONFIG = Dotenv.configure().load();
    public static final Logger LOGGER = Logger.getGlobal();

    static {
        dbHandler = new MySQLHandler();
    }

    public static Dotenv getConfig() {
        return CONFIG;
    }

    public static void main(String[] args) {
        try {
            String token = CONFIG.get("TOKEN");
            if (token == null || token.isEmpty()) {
                LOGGER.severe("Could not find bot token.");
                return;
            }

            JDA api = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setActivity(Activity.customStatus("Reviewing reviews.."))
                    .build();

            // Register listeners
            api.addEventListener(new ReviewListener(), new CommandManager());

            api.awaitReady();
        } catch (Exception e) {
            LOGGER.severe(String.format("An error occurred during bot initialization: %s", e.getMessage()));
        }
    }
}
