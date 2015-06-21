package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.core.NametagHandler;
import ca.wacos.nametagedit.core.NametagManager;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class PlayerSqlUpdate extends BukkitRunnable {

    private UUID uuid;

    @Override
    public void run() {
        Connection connection = null;

        String tempPrefix = null;
        String tempSuffix = null;

        try {
            connection = NametagEdit.getInstance().getHikari().getConnection();

            String query = "SELECT prefix,suffix FROM nte_players WHERE uuid=?";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, uuid.toString());

            ResultSet resultSet = p.executeQuery();

            if (resultSet.next()) {
                tempPrefix = resultSet.getString("prefix");
                tempSuffix = resultSet.getString("suffix");
            }
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

            final String prefix = tempPrefix == null ? "" : tempPrefix;
            final String suffix = tempSuffix == null ? "" : tempSuffix;

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player who = Bukkit.getPlayer(uuid);

                    if(who != null) {
                        NametagManager.overlap(who.getName(), prefix, suffix);
                    }
                }
            }.runTask(NametagEdit.getInstance());
        }
    }
}