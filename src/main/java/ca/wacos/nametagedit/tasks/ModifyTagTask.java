package ca.wacos.nametagedit.tasks;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.Messages;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.data.PlayerData;
import ca.wacos.nametagedit.utils.UUIDFetcher;

public class ModifyTagTask extends BukkitRunnable {

    private int id;
    
    private String player, value, uuid;
    
    private CommandSender sender;
    
    private NametagEdit plugin = NametagEdit.getInstance();
    
    public ModifyTagTask(CommandSender sender, String player, String value, int id) {
        this.sender = sender;
        this.player = player;
        this.value = value;
        this.id = id;
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
                if(uuid == null && sender != null) {
                    Messages.UUID_LOOKUP_FAILED.send(sender, player);
                } else {
                    if (!plugin.getNTEHandler().getPlayerData().containsKey(uuid)) {
                        plugin.getNTEHandler().getPlayerData().put(uuid, new PlayerData(player, uuid, "", ""));
                    } else {
                        PlayerData data = plugin.getNTEHandler().getPlayerData().get(uuid);
                        switch(id) {
                        case 1:
                            data.setPrefix(value);
                            break;
                        case 2:
                            data.setSuffix(value);
                            break;
                        }
                    }
                }
            }
        }.runTask(plugin);
    }
}