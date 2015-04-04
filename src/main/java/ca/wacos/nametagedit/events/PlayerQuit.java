package ca.wacos.nametagedit.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import ca.wacos.nametagedit.core.NametagManager;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        NametagManager.clear(e.getPlayer().getName());
    }
}