package ca.wacos.nametagedit.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagEdit;

public class TableCreatorTask extends BukkitRunnable {

    private List<String> queries = new ArrayList<>();
    
    public TableCreatorTask() {
        queries.add("CREATE TABLE IF NOT EXISTS `players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`uuid`));");
        queries.add("CREATE TABLE IF NOT EXISTS `groups` (`name` varchar(64) NOT NULL, `permission` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`name`));");
    }
    
    @Override
    public void run() {
        Connection connection = null;
        
        try {
            connection = NametagEdit.getInstance().getConnectionPool().getConnection();
            
            PreparedStatement insert = null;
            
            for(String s : queries) {
                insert = connection.prepareStatement(s);
                insert.execute();
            }
            
            insert.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}