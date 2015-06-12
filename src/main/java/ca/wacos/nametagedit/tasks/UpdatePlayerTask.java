package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class UpdatePlayerTask extends BukkitRunnable {

    private UUID uuid;
    private String name;
    private String prefix;
    private String suffix;

    @Override
    public void run() {
        Connection connection = null;

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
        }
    }
}