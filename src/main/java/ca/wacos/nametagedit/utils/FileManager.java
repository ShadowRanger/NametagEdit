package ca.wacos.nametagedit.utils;

import ca.wacos.nametagedit.NametagEdit;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * This class is responsible for loading/writing/saving files
 *
 * @author sgtcaze
 *
 */
public class FileManager {

    private final NametagEdit plugin = NametagEdit.getInstance();

    private File groupsFile, playersFile;
    private YamlConfiguration groups, players;

    public YamlConfiguration getGroupsFile() {
        return this.groups;
    }

    public YamlConfiguration getPlayersFile() {
        return this.players;
    }

    public void saveAllFiles() {
        try {
            players.save(playersFile);
            groups.save(groupsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFiles() {
        File groupsTemp = new File(plugin.getDataFolder(), "groups.yml");

        if (!groupsTemp.exists()) {
            File oldGroups = new File(plugin.getDataFolder(), "groups.txt");
            if (oldGroups.exists()) {
                try {
                    convertOldVersion();
                } catch (IOException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                generateFile("groups.yml");
            }
        }

        groupsFile = groupsTemp;
        groups = YamlConfiguration.loadConfiguration(groupsFile);

        File playersTemp = new File(plugin.getDataFolder(), "players.yml");

        if (!playersTemp.exists()) {
            generateFile("players.yml");
        }

        playersFile = playersTemp;
        players = YamlConfiguration.loadConfiguration(playersFile);
    }

    // Quick replacement for "FileUtils"
    private void generateFile(String name) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = plugin.getResource(name);
            outputStream = new FileOutputStream(new File(plugin.getDataFolder() + "/" + name));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void convertOldVersion() throws IOException {
        plugin.getLogger().info("Starting conversion from old format...");
        plugin.getLogger().info("Converting groups...");
        convertGroups();
        plugin.getLogger().info("Groups converted.");
        plugin.getLogger().info("Converting players...");
        convertUsers();
        plugin.getLogger().info("Players converted...");
        plugin.getLogger().info("Conversion from old format to new complete.");
    }

    private void convertGroups() throws IOException {
        File oldFile = new File(plugin.getDataFolder(), "groups.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(oldFile))) {
            File newFile = new File(plugin.getDataFolder(), "groups.yml");
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(newFile);

            List<String> order = new LinkedList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) {
                    continue;
                }
                String[] split = line.split("=");
                String prefix = split[1].trim().split("\"")[1];
                String[] splot = split[0].trim().split(" ");
                String perm = splot[0];
                String type = splot[1];
                String group = perm.substring(perm.indexOf(".") + 1);
                order.add(group);

                conf.set("Groups." + group + ".Permission", perm);
                conf.set("Groups." + group + "." + type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase(), prefix);
            }
            conf.set("Order", order);
            conf.save(newFile);
        }
        Files.move(oldFile, new File(plugin.getDataFolder(), "groups_old.txt"));
    }

    @SuppressWarnings("deprecation")
    private void convertUsers() throws IOException {
        File oldFile = new File(plugin.getDataFolder(), "players.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(oldFile))) {
            File newFile = new File(plugin.getDataFolder(), "players.yml");
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(newFile);
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("=")) {
                    continue;
                }
                String[] split = line.split("=");
                String prefix = split[1].trim().split("\"")[1];
                String[] splot = split[0].trim().split(" ");
                String playername = splot[0];
                String type = splot[1];

                OfflinePlayer op = plugin.getServer().getOfflinePlayer(playername);
                String uuid = op.getUniqueId().toString();
                conf.set("Players." + uuid + ".Name", playername);
                conf.set("Players." + uuid + "." + type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase(), prefix);
            }
            conf.save(newFile);
        }
        Files.move(oldFile, new File(plugin.getDataFolder(), "players_old.txt"));
    }
}