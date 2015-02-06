package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdatePlayerTask extends BukkitRunnable {

    private final String uuid, name, prefix, suffix;

    public UpdatePlayerTask(String uuid, String name, String prefix, String suffix) {
        this.uuid = uuid;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public void run() {
        Connection connection = null;

        try {
            connection = NametagEdit.getInstance().getConnectionPool().getConnection();

            String query = "INSERT INTO `players` VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?;";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, uuid);
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
        }
    }
}
