package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.Messages;
import ca.wacos.nametagedit.NametagEdit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import ca.wacos.nametagedit.data.PlayerData;
import ca.wacos.nametagedit.utils.UUIDFetcher;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class UpdatePlayerTask extends BukkitRunnable {

    private UUID uuid;
    private String name;
    private String prefix;
    private String suffix;
    private CommandSender sender;
    private int id;

    private final NametagEdit plugin = NametagEdit.getInstance();

    @Override
    public void run() {
        Connection connection = null;

        if (uuid == null) {
            try {
                uuid = UUIDFetcher.getUUIDOf(name);
            } catch (Exception e) {
                return; // Woah
            }
        }

        try {
            connection = NametagEdit.getInstance().getHikari().getConnection();

            String query = "INSERT INTO `nte_players` VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, uuid.toString());
            p.setString(2, name);
            p.setString(3, prefix);
            p.setString(4, suffix);
            p.setString(5, prefix);
            p.setString(6, suffix);
            p.execute();
            p.close();
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
                    syncMethod(uuid);
                }
            }.runTask(plugin);
        }
    }

    private void syncMethod(UUID uuid) {
        if (uuid == null && sender != null) {
            Messages.UUID_LOOKUP_FAILED.send(sender, name);
        } else {
            PlayerData data = plugin.getNteHandler().getPlayerData().get(uuid);

            if (data == null) {
                plugin.getNteHandler().getPlayerData().put(uuid, new PlayerData(name, uuid, "", ""));
            } else {
                switch (id) {
                    case 1:
                        data.setPrefix(prefix);
                        break;
                    case 2:
                        data.setSuffix(suffix);
                        break;
                }
            }

            if (sender != null) {
                Messages.OPERATION_COMPLETED.send(sender);
            }
        }
    }
}