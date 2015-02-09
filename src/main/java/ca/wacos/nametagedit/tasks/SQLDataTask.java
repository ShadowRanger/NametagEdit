package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.data.GroupData;
import ca.wacos.nametagedit.data.PlayerData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class is responsible for grabbing all data from the database and caching
 * it
 *
 * @author sgtcaze
 */
public class SQLDataTask extends BukkitRunnable {

    private final NametagEdit plugin = NametagEdit.getInstance();

    @Override
    public void run() {
        Connection connection = null;

        final HashMap<String, GroupData> groupDataTemp = new HashMap<>();
        final HashMap<String, PlayerData> playerDataTemp = new HashMap<>();

        String groupQuery = "SELECT * FROM `groups`;";
        String playerQuery = "SELECT * FROM `players`;";

        try {
            connection = plugin.getHikari().getConnection();

            ResultSet results = connection.prepareStatement(groupQuery).executeQuery();

            GroupData groupData;

            while (results.next()) {
                groupData = new GroupData(results.getString("name"), results.getString("prefix"), results.getString("suffix"), results.getString("permission"));
                groupDataTemp.put(results.getString("name"), groupData);
            }

            results = connection.prepareStatement(playerQuery).executeQuery();

            PlayerData playerData;

            while (results.next()) {
                playerData = new PlayerData(results.getString("name"), results.getString("uuid"), colorize(results.getString("prefix")), colorize(results.getString("suffix")));
                playerDataTemp.put(results.getString("name"), playerData);
            }

            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getLogger().info("[MySQL] Found " + groupDataTemp.size() + " groups");
                    plugin.getLogger().info("[MySQL] Found " + playerDataTemp.size() + " players");

                    plugin.getNteHandler().setGroupDataMap(groupDataTemp);
                    plugin.getNteHandler().setPlayerDataMap(playerDataTemp);

                    plugin.getNteHandler().getAllGroups().clear();

                    for (String s : groupDataTemp.keySet()) {
                        plugin.getNteHandler().getAllGroups().add(s);
                    }

                    plugin.getNteHandler().applyTags();
                }
            }.runTask(plugin);
        }
    }

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}