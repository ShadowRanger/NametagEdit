package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class AddGroupTask extends BukkitRunnable {

    private String groupName;
    private String permission;
    private String prefix;
    private String suffix;

    @Override
    public void run() {
        Connection connection = null;

        try {
            connection = NametagEdit.getInstance().getHikari().getConnection();

            String query = "INSERT INTO `nte_groups` VALUES(?, ?, ?, ?)";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, groupName);
            p.setString(2, permission);
            p.setString(3, prefix);
            p.setString(4, suffix);
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