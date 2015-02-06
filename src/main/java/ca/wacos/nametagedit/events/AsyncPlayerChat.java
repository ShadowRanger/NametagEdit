package ca.wacos.nametagedit.events;

import ca.wacos.nametagedit.NametagAPI;
import ca.wacos.nametagedit.NametagEdit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChat implements Listener {

    private final String format;

    private final NametagEdit plugin = NametagEdit.getInstance();

    public AsyncPlayerChat() {
        this.format = plugin.getConfig().getString("Chat.Format");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        String name = e.getPlayer().getName();

        String prefix = NametagAPI.getPrefix(name);
        String suffix = NametagAPI.getSuffix(name);

        String temp = format.replace("%prefix%", prefix).replace("%suffix%", suffix);

        e.setFormat(temp);
    }
}
