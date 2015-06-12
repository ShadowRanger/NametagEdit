package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

public class TableCreatorTask extends BukkitRunnable {

    private final List<String> queries = new ArrayList<String>() {{
        add("CREATE TABLE IF NOT EXISTS `nte_players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`uuid`))");
        add("CREATE TABLE IF NOT EXISTS `nte_groups` (`name` varchar(64) NOT NULL, `permission` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`name`))");
    }};

    @Override
    public void run() {
        Connection connection = null;

        try {
            connection = NametagEdit.getInstance().getHikari().getConnection();

            PreparedStatement insert = null;

            for (String query : queries) {
                insert = connection.prepareStatement(query);
                insert.execute();
            }

            insert.close();
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