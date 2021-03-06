package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class UpdateGroupTask extends BukkitRunnable {

    private String table;
    private String group;
    private String field;
    private String oper;

    @Override
    public void run() {
        Connection connection = null;

        try {
            connection = NametagEdit.getInstance().getHikari().getConnection();

            String query = "UPDATE `nte_" + table + "` SET `" + field + "`=? WHERE `name`=?";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, oper);
            p.setString(2, group);
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