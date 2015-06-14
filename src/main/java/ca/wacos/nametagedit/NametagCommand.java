package ca.wacos.nametagedit;

import ca.wacos.nametagedit.constants.NametagChangeReason;
import ca.wacos.nametagedit.constants.NametagChangeType;
import ca.wacos.nametagedit.constants.NametagEditPerms;
import ca.wacos.nametagedit.core.NametagHandler;
import ca.wacos.nametagedit.core.NametagManager;
import ca.wacos.nametagedit.data.GroupData;
import ca.wacos.nametagedit.data.PlayerData;
import ca.wacos.nametagedit.tasks.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.UUID;

/**
 * This class is responsible for handling the /ne command.
 *
 * @author Levi Webb Heavily edited by @sgtcaze
 */
@SuppressWarnings("deprecation")
public class NametagCommand implements CommandExecutor {

    private final NametagEdit plugin = NametagEdit.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!NametagEditPerms.NTE_USE.hasPermission(sender)) {
            Messages.NO_PERMISSION.send(sender);
            return false;
        }

        if (args.length < 1) {
            Messages.COMMAND_USAGE.send(sender);
        } else if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "clear":
                    cmdClear(sender, args);
                    break;
                case "reload":
                    cmdReload(sender, args);
                    break;
                case "prefix":
                case "suffix":
                    cmdEdit(sender, args);
                    break;
                case "groups":
                    cmdGroups(sender, args);
                    break;
                default:
                    Messages.UNRECOGNIZED_VALUE.send(sender, args[0]);
                    break;
            }
        }

        return false;
    }

    // Clears prefixes and suffixes
    private void cmdClear(final CommandSender sender, String[] args) {
        if (!(NametagEditPerms.NTE_CLEAR_SELF.hasPermission(sender) || NametagEditPerms.NTE_CLEAR_OTHERS.hasPermission(sender))) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }

        if (args.length != 2) {
            Messages.USAGE_CLEAR.send(sender);
        } else {
            String targetName = args[1];

            if (!NametagEditPerms.NTE_CLEAR_OTHERS.hasPermission(sender) && !targetName.equalsIgnoreCase(sender.getName())) {
                Messages.MODIFY_OWN_TAG.send(sender);
                return;
            }

            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                new ClearPlayerTask(targetName, sender).runTaskAsynchronously(plugin);
            } else {
                UUID uuid = target.getUniqueId();

                NametagManager.clear(target.getName());

                NametagHandler nametagHandler = plugin.getNteHandler();
                nametagHandler.getPlayerData().remove(uuid);

                if (nametagHandler.isUsingDatabase()) {
                    new DeleteTypeTask("players", "uuid", uuid.toString()).runTaskAsynchronously(plugin);
                }
            }
        }
    }

    // Reloads from file or memory
    private void cmdReload(CommandSender sender, String[] args) {
        if (!NametagEditPerms.NTE_RELOAD.hasPermission(sender)) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }

        if (args.length != 2) {
            Messages.USAGE_RELOAD.send(sender);
        } else if (args[1].equalsIgnoreCase("file")) {
            plugin.getNteHandler().reload(sender, true);
        } else if (args[1].equalsIgnoreCase("memory")) {
            plugin.getNteHandler().reload(sender, false);
        }
    }

    // Sets prefix or suffix
    private void cmdEdit(CommandSender sender, String[] args) {
        if (!(NametagEditPerms.NTE_EDIT_SELF.hasPermission(sender) || NametagEditPerms.NTE_EDIT_OTHERS.hasPermission(sender))) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }

        if (args.length <= 2) {
            Messages.USAGE_EDIT.send(sender);
        } else {
            String targetName = args[1];

            if (!NametagEditPerms.NTE_EDIT_OTHERS.hasPermission(sender) && !targetName.equalsIgnoreCase(sender.getName())) {
                Messages.MODIFY_OWN_TAG.send(sender);
                return;
            }

            String type = args[0].toLowerCase();

            Player target = Bukkit.getPlayer(args[1]);

            String oper = format(args, 2, args.length);

            setType(sender, targetName, type, NametagAPI.format(oper));

            if (plugin.getNteHandler().isUsingDatabase() && target != null) {
                new UpdatePlayerTask(target.getUniqueId(), target.getName(), NametagManager.getPrefix(target.getName()), NametagManager.getSuffix(target.getName())).runTaskAsynchronously(plugin);
            }
        }
    }

    // Groups subcommand
    private void cmdGroups(CommandSender sender, String[] args) {
        if (!NametagEditPerms.NTE_GROUPS.hasPermission(sender)) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }


        if (args.length < 2) {
            Messages.USAGE_GROUP.send(sender);
        } else if (args.length >= 2) {
            NametagHandler nametagHandler = plugin.getNteHandler();

            if (args[1].equalsIgnoreCase("list")) {
                String prefix = "&3NTE &4» &r";
                StringBuilder sb = new StringBuilder();

                for (GroupData groupData : nametagHandler.getGroupData()) {
                    sb.append(prefix + " Group: &c" + groupData.getGroupName() + " &fPermission: &c" + groupData.getPermission() + "\n" +
                            "&fPrefix: " + groupData.getPrefix() + "Notch &fSuffix: Notch " + groupData.getSuffix() + " Complete: "
                            + groupData.getPrefix() + "Notch " + groupData.getSuffix() + "\n");
                }

                sender.sendMessage(NametagAPI.colorize(sb.toString()));
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 3) {
                    String group = args[2];

                    for (GroupData groupData : nametagHandler.getGroupData()) {
                        if (groupData.getGroupName().equalsIgnoreCase(group)) {
                            nametagHandler.getGroupData().remove(groupData);

                            if (nametagHandler.isUsingDatabase()) {
                                new DeleteTypeTask("groups", "name", group).runTaskAsynchronously(plugin);
                            }

                            Messages.GROUP_REMOVED.send(sender, group);
                        }
                    }
                }
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String group = args[2];

                    for (GroupData groupData : nametagHandler.getGroupData()) {
                        if (groupData.getGroupName().equalsIgnoreCase(group)) {
                            Messages.GROUP_EXISTS.send(sender, group);
                            return;
                        }
                    }

                    if (nametagHandler.isUsingDatabase()) {
                        new AddGroupTask(group, "", "", "").runTaskAsynchronously(plugin);
                    }

                    nametagHandler.getGroupData().add(new GroupData(group, "", "", "", new Permission("my.perm", PermissionDefault.FALSE)));
                }
            } else if (args[1].equalsIgnoreCase("set")) {
                if (args.length >= 5) {
                    String group = args[2];

                    GroupData groupData = null;

                    for (GroupData groups : nametagHandler.getGroupData()) {
                        if (groups.getGroupName().equalsIgnoreCase(group)) {
                            groupData = groups;
                            break;
                        }
                    }

                    if (groupData == null) {
                        Messages.GROUP_EXISTS_NOT.send(sender, group);
                        return;
                    }

                    if (args[3].equalsIgnoreCase("perm")) {
                        groupData.setPermission(args[4]);

                        Messages.GROUP_VALUE.sendMulti(sender, group, "permission", args[4]);

                        if (plugin.getNteHandler().isUsingDatabase()) {
                            new UpdateGroupTask("groups", "permission", group, args[4]).runTaskAsynchronously(plugin);
                        }
                    } else if (args[3].equalsIgnoreCase("prefix")) {
                        String oper = format(args, 4, args.length).replace("\"", "");

                        groupData.setPrefix(NametagAPI.format(oper));

                        Messages.GROUP_VALUE.sendMulti(sender, group, "prefix", NametagAPI.format(oper));

                        if (plugin.getNteHandler().isUsingDatabase()) {
                            new UpdateGroupTask("groups", "prefix", group, NametagAPI.format(oper)).runTaskAsynchronously(plugin);
                        }
                    } else if (args[3].equalsIgnoreCase("suffix")) {
                        String oper = format(args, 4, args.length).replace("\"", "");;

                        groupData.setSuffix(NametagAPI.format(oper));

                        Messages.GROUP_VALUE.sendMulti(sender, group, "suffix", NametagAPI.format(oper));

                        if (plugin.getNteHandler().isUsingDatabase()) {
                            new UpdateGroupTask("groups", "suffix", group, NametagAPI.format(oper)).runTaskAsynchronously(plugin);
                        }
                    }
                } else {
                    Messages.GROUP_USAGE.send(sender);
                }
            }
        }
    }

    /**
     * Updates the playerData hashmap and reloads the content async if the
     * player is offline (to get their UUID)
     *
     * @param sender     Sender of the command
     * @param targetName Target player name
     * @param type       Type to change
     * @param args       Value to change to
     */
    @SuppressWarnings("deprecation")
    public void setType(CommandSender sender, String targetName, String type, String args) {
        NametagChangeReason reason;

        int id;

        if (type.equals("prefix")) {
            reason = NametagChangeReason.SET_PREFIX;
            id = 1;
        } else {
            reason = NametagChangeReason.SET_SUFFIX;
            id = 2;
        }

        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            new ModifyTagTask(id, targetName, args, sender).runTaskAsynchronously(plugin);
        } else {
            UUID uuid = target.getUniqueId();

            PlayerData data = plugin.getNteHandler().getPlayerData().get(uuid);

            if (data == null) {
                plugin.getNteHandler().getPlayerData().put(target.getUniqueId(), new PlayerData(targetName, target.getUniqueId(), "", ""));
            } else {
                switch (id) {
                    case 1:
                        data.setPrefix(args);
                        break;
                    case 2:
                        data.setSuffix(args);
                        break;
                }
            }

            if (reason == NametagChangeReason.SET_PREFIX) {
                setNametagSoft(target.getName(), args, "", reason);
            } else {
                setNametagSoft(target.getName(), "", args, reason);
            }
        }
    }

    private String format(String[] text, int to, int from) {
        return StringUtils.join(text, ' ', to, from).replace("'", "");
    }

    /**
     * Sets a player's nametag with the given information and additional reason.
     *
     * @param player the player whose nametag to set
     * @param prefix the prefix to set
     * @param suffix the suffix to set
     * @param reason the reason for setting the nametag
     */
    public static void setNametagSoft(String player, String prefix, String suffix, NametagChangeReason reason) {
        NametagChangeEvent e = new NametagChangeEvent(player, NametagAPI.getPrefix(player), NametagAPI.getSuffix(player), prefix, suffix, NametagChangeType.SOFT, reason);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if (!e.isCancelled()) {
            NametagManager.update(player, prefix, suffix);
        }
    }
}