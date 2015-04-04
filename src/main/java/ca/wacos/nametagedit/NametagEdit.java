package ca.wacos.nametagedit;

import java.io.IOException;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import ca.wacos.nametagedit.core.NametagHandler;
import ca.wacos.nametagedit.core.NametagManager;
import ca.wacos.nametagedit.events.AsyncPlayerChat;
import ca.wacos.nametagedit.events.PlayerJoin;
import ca.wacos.nametagedit.events.PlayerQuit;
import ca.wacos.nametagedit.tasks.SQLDataTask;
import ca.wacos.nametagedit.tasks.TableCreatorTask;
import ca.wacos.nametagedit.utils.FileManager;

import com.zaxxer.hikari.HikariDataSource;

/**
 * This is the main class for the NametagEdit plugin.
 *
 * @author sgtcaze
 *
 */
@Getter
public class NametagEdit extends JavaPlugin {

    @Getter
    private static NametagEdit instance;

    private Listener chatListener;

    private FileManager fileUtils;
    private NametagHandler nteHandler;
    private NametagManager nametagManager;

    private HikariDataSource hikari;

    @Override
    public void onEnable() {
        instance = this;

        fileUtils = new FileManager();
        nteHandler = new NametagHandler();
        nametagManager = new NametagManager();

        getCommand("ne").setExecutor(new NametagCommand());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new PlayerQuit(), this);

        FileConfiguration config = getConfig();

        if (config.getBoolean("Chat.Enabled")) {
            registerChatListener();
        }

        if (config.getBoolean("MetricsEnabled")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
        }

        saveDefaultConfig();
        fileUtils.loadFiles();

        NametagManager.load();

        if (nteHandler.usingDatabase()) {
            setupHikari();
            new TableCreatorTask().runTask(this);
            new SQLDataTask().runTask(this);
        } else {
            nteHandler.loadFromFile();
        }

        nteHandler.applyTags();
    }

    @Override
    public void onDisable() {
        NametagManager.reset();

        nteHandler.saveFileData();

        if (hikari != null) {
            hikari.shutdown();
        }
    }

    public void registerChatListener() {
        chatListener = new AsyncPlayerChat();
        Bukkit.getPluginManager().registerEvents(chatListener, this);
    }

    public void unregisterChatListener() {
        HandlerList.unregisterAll(chatListener);
    }

    private void setupHikari() {
        FileConfiguration config = getConfig();

        String address = config.getString("MySQL.Hostname");
        String name = config.getString("MySQL.Database");
        String username = config.getString("MySQL.Username");
        String password = config.getString("MySQL.Password");

        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(5);
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", address);
        hikari.addDataSourceProperty("port", "3306");
        hikari.addDataSourceProperty("databaseName", name);
        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);
    }
}