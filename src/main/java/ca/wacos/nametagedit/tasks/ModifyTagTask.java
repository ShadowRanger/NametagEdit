package ca.wacos.nametagedit.tasks;

import ca.wacos.nametagedit.Messages;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.data.PlayerData;
import ca.wacos.nametagedit.utils.UUIDFetcher;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@AllArgsConstructor
public class ModifyTagTask extends BukkitRunnable {

    private int id;

    private String player;
    private String value;

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
                syncMethod(uuid);
            }
        }.runTask(plugin);
    }

    private void syncMethod(UUID uuid) {
        if (uuid == null && sender != null) {
            Messages.UUID_LOOKUP_FAILED.send(sender, player);
        } else {
            PlayerData data = plugin.getNteHandler().getPlayerData().get(uuid);

            if (data == null) {
                plugin.getNteHandler().getPlayerData().put(uuid, new PlayerData(player, uuid, "", ""));
            } else {
                switch (id) {
                    case 1:
                        data.setPrefix(value);
                        break;
                    case 2:
                        data.setSuffix(value);
                        break;
                }
            }

            if (sender != null) {
                Messages.OPERATION_COMPLETED.send(sender);
            }
        }
    }
}