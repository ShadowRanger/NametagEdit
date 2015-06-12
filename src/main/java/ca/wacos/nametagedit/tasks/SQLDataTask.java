package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagAPI;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.core.NametagHandler;
import ca.wacos.nametagedit.data.GroupData;
import ca.wacos.nametagedit.data.PlayerData;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This class is responsible for grabbing all data from the database and caching
 * it
 *
 * @author sgtcaze
 */
public class SQLDataTask extends BukkitRunnable {

    private final NametagEdit plugin = NametagEdit.getInstance();

    private static final String GROUP_QUERY = "SELECT name, prefix, suffix, permission FROM nte_groups";
    private static final String PLAYER_QUERY = "SELECT name, uuid, prefix, suffix FROM nte_players";

    @Override
    public void run() {
        final List<GroupData> groupData = new ArrayList<>();
        final Map<UUID, PlayerData> playerData = new HashMap<>();

        Connection connection = null;

        try {
            connection = plugin.getHikari().getConnection();

            ResultSet results = connection.prepareStatement(GROUP_QUERY).executeQuery();

            while (results.next()) {
                groupData.add(new GroupData(results.getString("name"), results.getString("prefix"), results.getString("suffix"), results.getString("permission")));
            }

            results = connection.prepareStatement(PLAYER_QUERY).executeQuery();

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                playerData.put(uuid, new PlayerData(results.getString("name"), uuid, NametagAPI.colorize(results.getString("prefix")), NametagAPI.colorize(results.getString("suffix"))));
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
                    syncTask(groupData, playerData);
                }
            }.runTask(plugin);
        }
    }

    private void syncTask(List<GroupData> groupData, Map<UUID, PlayerData> playerData) {
        plugin.getLogger().info("[MySQL] Found " + groupData.size() + " groups");
        plugin.getLogger().info("[MySQL] Found " + playerData.size() + " players");

        NametagHandler nametagHandler = plugin.getNteHandler();
        nametagHandler.setGroupData(groupData);
        nametagHandler.setPlayerData(playerData);
        nametagHandler.applyTags();
    }
}