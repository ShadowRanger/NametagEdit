package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.Messages;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.utils.UUIDFetcher;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@AllArgsConstructor
public class ClearPlayerTask extends BukkitRunnable {

    private String player;
    private CommandSender sender;

    private final NametagEdit plugin = NametagEdit.getInstance();

    @Override
    public void run() {
        UUID tempUUID = null;

        try {
            tempUUID = UUIDFetcher.getUUIDOf(player);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to retrieve UUID for " + player);
        }

        final UUID uuid = tempUUID;

        new BukkitRunnable() {
            @Override
            public void run() {
                syncTask(uuid);
            }
        }.runTask(plugin);
    }

    private void syncTask(UUID uuid) {
        if (uuid == null && sender != null) {
            Messages.UUID_LOOKUP_FAILED.send(sender);
        } else {
            if (sender != null) {
                Messages.OPERATION_COMPLETED.send(sender);
            }

            plugin.getNteHandler().getPlayerData().remove(uuid);
        }
    }
}