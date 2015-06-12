package ca.wacos.nametagedit.core;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This class dynamically creates teams with numerical names and certain
 * prefixes/suffixes (it ignores teams with other characters) to assign unique
 * prefixes and suffixes to specific players in the game. This class makes edits
 * to the <b>scoreboard.dat</b> file, adding and removing teams on the fly.
 *
 * @author Levi Webb
 */
public class NametagManager {

    // Prefix to append to all team names (nothing to do with prefix/suffix)
    private static final String TEAM_NAME_PREFIX = "NTE";

    private static final List<Integer> LIST = new ArrayList<>();
    private static final List<String> EMPTY_LIST = new ArrayList<>();

    private static final HashMap<TeamHandler, List<String>> TEAMS = new HashMap<>();

    // Workaround for the deprecated getOnlinePlayers()
    public static List<Player> getOnline() {
        List<Player> list = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            list.addAll(world.getPlayers());
        }

        return Collections.unmodifiableList(list);
    }

    private static void register(TeamHandler team) {
        TEAMS.put(team, EMPTY_LIST);
        sendPacketsAddTeam(team);
    }

    private static void removeTeam(TeamHandler team) {
        sendPacketsRemoveTeam(team);
        TEAMS.remove(team);
    }

    private static Collection<TeamHandler> getTeams() {
        return TEAMS.keySet();
    }

    public static void clear(String playerName) {
        removeFromTeam(playerName);
    }

    private static List<String> getTeamPlayers(TeamHandler team) {
        List<String> list = TEAMS.get(team);
        return list == null ? new ArrayList<String>() : list;
    }

    private static TeamHandler getTeam(String teamName) {
        for (TeamHandler team : TEAMS.keySet()) {
            if (team.getName().equals(teamName)) {
                return team;
            }
        }
        return null;
    }

    private static void addToTeam(TeamHandler team, String playerName) {
        removeFromTeam(playerName);
        List<String> list = TEAMS.get(team);

        if (list != null) {
            list.add(playerName);
            Player toAdd = Bukkit.getPlayerExact(playerName);

            if (toAdd != null) {
                sendPacketsAddToTeam(team, toAdd.getName());
            } else {
                OfflinePlayer toAddOffline = Bukkit.getOfflinePlayer(playerName);
                sendPacketsAddToTeam(team, toAddOffline.getName());
            }
        }
    }

    private static TeamHandler removeFromTeam(String playerName) {
        for (Map.Entry<TeamHandler, List<String>> entry : TEAMS.entrySet()) {
            TeamHandler team = entry.getKey();
            Iterator<String> list = entry.getValue().iterator();

            while (list.hasNext()) {
                String temp = list.next();

                if (temp.equals(playerName)) {
                    Player toRemove = Bukkit.getPlayerExact(playerName);

                    if (toRemove != null) {
                        sendPacketsRemoveFromTeam(team, toRemove.getName());
                    } else {
                        OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayer(temp);
                        sendPacketsRemoveFromTeam(team, toRemoveOffline.getName());
                    }

                    list.remove();
                    return team;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the player's entire name with both the prefix and suffix.
     *
     * @param player the specified player
     * @return the entire nametag
     */
    public static String getFormattedName(String player) {
        return getPrefix(player) + player + getSuffix(player);
    }

    /**
     * Initializes this class and loads current teams that are manipulated by
     * this plugin.
     */
    public static void load() {
        for (TeamHandler team : getTeams()) {
            int entry = -1;

            try {
                entry = Integer.parseInt(team.getName());
            } catch (NumberFormatException e) {
                // We're ignoring this. I don't know why.
            }

            if (entry != -1) {
                LIST.add(entry);
            }
        }
    }

    /**
     * Updates a player's prefix and suffix in the scoreboard and above their
     * head.<br>
     * <br>
     * <p/>
     * If either the prefix or suffix is null or empty, it will be replaced with
     * the current prefix/suffix
     *
     * @param player the specified player
     * @param prefix the prefix to set for the given player
     * @param suffix the suffix to set for the given player
     */
    public static void update(String player, String prefix, String suffix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = getPrefix(player);
        }

        if (suffix == null || suffix.isEmpty()) {
            suffix = getSuffix(player);
        }

        addToTeam(getTeamHandler(prefix, suffix), player);
    }

    /**
     * Updates a player's prefix and suffix in the scoreboard and above their
     * head.<br>
     * <br>
     * <p/>
     * If either the prefix or suffix is null or empty, it will be removed from
     * the player's nametag.
     *
     * @param player the specified player
     * @param prefix the prefix to set for the given player
     * @param suffix the suffix to set for the given player
     */
    public static void overlap(String player, String prefix, String suffix) {
        if (prefix == null) {
            prefix = "";
        }

        if (suffix == null) {
            suffix = "";
        }

        addToTeam(getTeamHandler(prefix, suffix), player);
    }

    /**
     * Retrieves a player's prefix
     *
     * @param player the specified player
     * @return the player's prefix
     */
    public static String getPrefix(String player) {
        for (Map.Entry<TeamHandler, List<String>> entry : TEAMS.entrySet()) {
            for (String member : entry.getValue()) {
                if (member.equals(player)) {
                    return entry.getKey().getPrefix();
                }
            }
        }
        return "";
    }

    /**
     * Retrieves a player's suffix
     *
     * @param player the specified player
     * @return the player's suffix
     */
    public static String getSuffix(String player) {
        for (Map.Entry<TeamHandler, List<String>> entry : TEAMS.entrySet()) {
            for (String member : entry.getValue()) {
                if (member.equals(player)) {
                    return entry.getKey().getSuffix();
                }
            }
        }
        return "";
    }

    /**
     * Declares a new team in the scoreboard.dat of the given main world.
     *
     * @param name   the team name
     * @param prefix the team's prefix
     * @param suffix the team's suffix
     * @return the created team
     */
    private static TeamHandler declareTeam(String name, String prefix, String suffix) {
        removeTeam(getTeam(name));

        TeamHandler team = new TeamHandler(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        register(team);

        return team;
    }

    /**
     * Gets the ScoreboardTeam for the
     * given prefix and suffix, and if none matches, creates a new team with the
     * provided info. This also removes teams that currently have no players.
     *
     * @param prefix the team's prefix
     * @param suffix the team's suffix
     * @return a team with the corresponding prefix/suffix
     */
    private static TeamHandler getTeamHandler(String prefix, String suffix) {
        update();

        for (int index : LIST) {
            TeamHandler team = getTeam(TEAM_NAME_PREFIX + index);

            if (team != null) {
                if (team.getSuffix().equalsIgnoreCase(suffix) && team.getPrefix().equals(prefix)) {
                    return team;
                }
            }
        }
        return declareTeam(TEAM_NAME_PREFIX + nextName(), prefix, suffix);
    }

    /**
     * Returns the next available team name that is not taken.
     *
     * @return an integer that for a team name that is not taken.
     */
    private static int nextName() {
        int index = 0;

        while (LIST.contains(index)) {
            index++;
        }

        LIST.add(index);
        return index;
    }

    /**
     * Removes any teams that do not have any players in them.
     */
    private static void update() {
        for (Map.Entry<TeamHandler, List<String>> entry : TEAMS.entrySet()) {
            TeamHandler team = entry.getKey();
            List<String> members = entry.getValue();

            int id;

            try {
                id = Integer.parseInt(team.getName());
            } catch (NumberFormatException e) {
                continue; // Dead team?
            }

            if (id != -1) {
                if (members.isEmpty()) {
                    removeTeam(team);
                    LIST.remove(id);
                }
            }
        }
    }

    /**
     * Sends the current team setup and their players to the given player. This
     * should be called when players join the server.
     *
     * @param p The player to send the packets to.
     */
    public static void sendTeamsToPlayer(Player p) {
        try {
            for (TeamHandler team : getTeams()) {
                PacketHandler mod = new PacketHandler(team.getName(), team.getPrefix(), team.getSuffix(), EMPTY_LIST, 0);
                mod.sendToPlayer(p);
                mod = new PacketHandler(team.getName(), getTeamPlayers(team), 3);
                mod.sendToPlayer(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends packets out to players to add the given team
     *
     * @param team the team to add
     */
    private static void sendPacketsAddTeam(TeamHandler team) {
        try {
            for (Player p : getOnline()) {
                if (p != null) {
                    PacketHandler mod = new PacketHandler(team.getName(), team.getPrefix(), team.getSuffix(), EMPTY_LIST, 0);
                    mod.sendToPlayer(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends packets out to players to remove the given team
     *
     * @param team the team to remove
     */
    private static void sendPacketsRemoveTeam(TeamHandler team) {
        if(!TEAMS.containsKey(team)) {
            return;
        }

        try {
            for (Player player : getOnline()) {
                if (player != null) {
                    PacketHandler mod = new PacketHandler(team.getName(), team.getPrefix(), team.getSuffix(), EMPTY_LIST, 1);
                    mod.sendToPlayer(player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends out packets to players to add the given player to the given team
     *
     * @param team the team to use
     * @param playerName the player to add
     */
    private static void sendPacketsAddToTeam(TeamHandler team, String playerName) {
        if(!TEAMS.containsKey(team)) {
            return;
        }

        try {
            for (Player player : getOnline()) {
                if (player != null) {
                    PacketHandler mod = new PacketHandler(team.getName(), Arrays.asList(playerName), 3);
                    mod.sendToPlayer(player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends out packets to players to remove the given player from the given
     * team.
     *
     * @param team   the team to remove from
     * @param playerName the player to remove
     */
    private static void sendPacketsRemoveFromTeam(TeamHandler team, String playerName) {
        if(!TEAMS.containsKey(team)) {
            return;
        }

        if(!TEAMS.get(team).contains(playerName)) {
            return;
        }

        try {
            for (Player player : getOnline()) {
                if (player != null) {
                    PacketHandler mod = new PacketHandler(team.getName(), Arrays.asList(playerName), 4);
                    mod.sendToPlayer(player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears out all teams and removes them for all the players. Called when
     * the plugin is disabled.
     */
    public static void reset() {
        Collection<TeamHandler> teams = new ArrayList<>(getTeams());
        for (TeamHandler team : teams) {
            removeTeam(team);
        }
    }
}