package com.conan.bots.review;

import com.conan.bots.review.datastore.MySQLHandler;
import com.conan.bots.review.listeners.ReviewListener;
import com.conan.bots.review.listeners.commands.CommandManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.util.logging.Logger;

import static com.conan.bots.review.datastore.DatabaseHandler.dbHandler;

public class ReviewBot {

    private static ShardManager shardManager = null;

    private static final Dotenv CONFIG = Dotenv.configure().load();
    public static final Logger LOGGER = Logger.getGlobal();


    static {
        dbHandler = new MySQLHandler();
    }


    public ReviewBot() throws LoginException {
        String token = CONFIG.get("TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.setActivity(Activity.customStatus("Reviewing reviews..."));

        shardManager = builder.build();

//        Register listeners
        shardManager.addEventListener(new ReviewListener(), new CommandManager());
    }

    public static void main(String[] args) {
        try {
            ReviewBot bot = new ReviewBot();
        } catch (LoginException e) {
            System.out.println("ERROR: Provided bot token is invalid!");
        }
    }

    public static Dotenv getConfig() {
        return CONFIG;
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }
}
