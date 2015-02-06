package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.Messages;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.utils.UUIDFetcher;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ClearPlayerTask extends BukkitRunnable {

    private final String player;
    private String uuid;

    private final CommandSender sender;

    private final NametagEdit plugin = NametagEdit.getInstance();

    public ClearPlayerTask(CommandSender sender, String player) {
        this.sender = sender;
        this.player = player;
    }

    @Override
    public void run() {
        try {
            uuid = UUIDFetcher.getUUIDOf(player).toString();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to retrieve UUID for " + player);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (uuid == null && sender != null) {
                    Messages.UUID_LOOKUP_FAILED.send(sender);
                } else {
                    if (sender != null) {
                        // Send info that
                    }

                    plugin.getNTEHandler().getPlayerData().remove(uuid);
                }
            }
        }.runTask(plugin);
    }
}
