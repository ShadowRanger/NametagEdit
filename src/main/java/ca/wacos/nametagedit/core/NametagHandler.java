package ca.wacos.nametagedit.core;

import ca.wacos.nametagedit.Messages;
import ca.wacos.nametagedit.NametagAPI;
import ca.wacos.nametagedit.NametagCommand;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.constants.NametagChangeReason;
import ca.wacos.nametagedit.data.GroupData;
import ca.wacos.nametagedit.data.PlayerData;
import ca.wacos.nametagedit.events.PlayerJoinUpdater;
import ca.wacos.nametagedit.tasks.SQLDataTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * This class loads all group/player data, and applies the tags during
 * reloads/individually
 *
 * @author sgtcaze
 */
@Getter
@Setter
public class NametagHandler {

    private final boolean usingDatabase;
    private final boolean tabListDisabled;

    // Stores all "GroupData" classes in order
    private List<GroupData> groupData = new ArrayList<>();

    // Stores all player names to prefix/suffix
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    private final NametagEdit plugin = NametagEdit.getInstance();

    public NametagHandler(FileConfiguration config) {
        this.usingDatabase = config.getBoolean("MySQL.Enabled");
        this.tabListDisabled = config.getBoolean("TabListDisabled");

        if (config.getBoolean("MySQL.Updating.OnJoin")) {
            Bukkit.getPluginManager().registerEvents(new PlayerJoinUpdater(), plugin);
        } else if (config.getBoolean("MySQL.Updating.OnTimer")) {
            int seconds = config.getInt("MySQL.Updating.TimerInterval");

            if (seconds == -1 || seconds < 0) {
                seconds = 60;
            }

            final int ticks = seconds * 20;

            new BukkitRunnable() {
                @Override
                public void run() {
                    reload(null, false);
                }
            }.runTaskTimer(plugin, 0, ticks);
        }
    }

    private void setBlankTag(Player p) {
        p.setPlayerListName(NametagAPI.format("&f" + p.getName()));
    }

    // Reloads the current storage mechanism, and reapplies tags
    public void reload(CommandSender sender, boolean fromFile) {
        if (usingDatabase) {
            new SQLDataTask().runTaskAsynchronously(plugin); // Load from the database
        } else {
            if (fromFile) {
                plugin.reloadConfig();
                plugin.getFileUtils().loadFiles();
            } else {
                plugin.saveConfig();
                saveFileData();
            }

            if (plugin.getConfig().getBoolean("Chat.Enabled")) {
                if (plugin.getChatListener() == null) {
                    plugin.registerChatListener();
                }
            } else {
                plugin.unregisterChatListener();
            }

            loadFromFile();
            applyTags();
        }

        if (sender != null) {
            Messages.RELOADED_DATA.send(sender);
        }
    }

    private String reformat(String input) {
        return input.replace("§", "&");
    }

    // Saves all player and group data
    public void saveFileData() {
        YamlConfiguration playersFile = plugin.getFileUtils().getPlayers();
        YamlConfiguration groupsFile = plugin.getFileUtils().getGroups();

        for (PlayerData data : playerData.values()) {
            UUID uuid = data.getUuid();
            String name = data.getName();
            playersFile.set("Players." + uuid + ".Name", name);
            playersFile.set("Players." + uuid + ".Prefix", reformat(data.getPrefix()));
            playersFile.set("Players." + uuid + ".Suffix", reformat(data.getSuffix()));
        }

        for (GroupData data : groupData) {
            String name = data.getGroupName();
            groupsFile.set("Groups." + name + ".Permission", data.getPermission());
            groupsFile.set("Groups." + name + ".Prefix", reformat(data.getPrefix()));
            groupsFile.set("Groups." + name + ".Suffix", reformat(data.getSuffix()));
        }

        plugin.getFileUtils().saveAllFiles();
    }

    // Loads all player and group data (file)
    public void loadFromFile() {
        YamlConfiguration playersFile = plugin.getFileUtils().getPlayers();
        YamlConfiguration groupsFile = plugin.getFileUtils().getGroups();

        groupData.clear();
        playerData.clear();

        for (String key : groupsFile.getConfigurationSection("Groups").getKeys(false)) {
            GroupData data = new GroupData();
            data.setGroupName(key);
            data.setPermission(groupsFile.getString("Groups." + key + ".Permission"));
            data.setPrefix(groupsFile.getString("Groups." + key + ".Prefix", ""));
            data.setSuffix(groupsFile.getString("Groups." + key + ".Suffix", ""));
            data.refresh();
            groupData.add(data);
        }

        for (String uuid : playersFile.getConfigurationSection("Players").getKeys(false)) {
            PlayerData data = new PlayerData();
            data.setName(playersFile.getString("Players." + uuid + ".Name"));
            data.setUuid(UUID.fromString(uuid));
            data.setPrefix(playersFile.getString("Players." + uuid + ".Prefix", ""));
            data.setSuffix(playersFile.getString("Players." + uuid + ".Suffix", ""));
            playerData.put(UUID.fromString(uuid), data);
        }
    }

    // Applies tags to online players (for /reload, and /ne reload)
    public void applyTags() {
        for (Player p : NametagManager.getOnline()) {
            if (p == null) {
                continue;
            }

            NametagManager.clear(p.getName());

            UUID uuid = p.getUniqueId();

            PlayerData data = playerData.get(uuid);

            if (data != null) {
                NametagManager.overlap(p.getName(), NametagAPI.format(data.getPrefix()), NametagAPI.format(data.getSuffix()));
            } else {
                for (GroupData group : groupData) {
                    if (p.hasPermission(group.getBukkitPermission())) {
                        NametagCommand.setNametagSoft(p.getName(), NametagAPI.format(group.getPrefix()), NametagAPI.format(group.getSuffix()), NametagChangeReason.GROUP_NODE);
                        break;
                    }
                }
            }

            if (tabListDisabled) {
                setBlankTag(p);
            }
        }
    }

    // Applies tags to a specific player
    public void applyTagToPlayer(Player p) {
        UUID uuid = p.getUniqueId();

        NametagManager.clear(p.getName());

        PlayerData data = playerData.get(uuid);

        if (data != null) {
            NametagManager.overlap(p.getName(), NametagAPI.format(data.getPrefix()), NametagAPI.format(data.getSuffix()));
        } else {
            for (GroupData group : groupData) {
                if (p.hasPermission(group.getBukkitPermission())) {
                    NametagCommand.setNametagSoft(p.getName(), NametagAPI.format(group.getPrefix()), NametagAPI.format(group.getSuffix()), NametagChangeReason.GROUP_NODE);
                    break;
                }
            }
        }

        if (tabListDisabled) {
            setBlankTag(p);
        }
    }
}