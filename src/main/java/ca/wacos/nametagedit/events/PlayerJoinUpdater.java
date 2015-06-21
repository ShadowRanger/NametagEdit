package ca.wacos.nametagedit.events;

import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.tasks.PlayerSqlUpdate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinUpdater implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        new PlayerSqlUpdate(e.getPlayer().getUniqueId()).runTaskAsynchronously(NametagEdit.getInstance());
    }
}
