package ca.wacos.nametagedit.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.YamlConfiguration;

import ca.wacos.nametagedit.NametagEdit;

/**
 * This class is responsible for loading/writing/saving files
 * 
 * @author sgtcaze
 * 
 */
public class FileManager {

    private NametagEdit plugin = NametagEdit.getInstance();

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
            generateFile("groups.yml");
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
}