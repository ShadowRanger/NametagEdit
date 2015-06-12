package ca.wacos.nametagedit.constants;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum NametagEditPerms {

    NTE_USE("nametagedit.use"),
    NTE_EDIT_SELF("nametagedit.edit.self"),
    NTE_EDIT_OTHERS("nametagedit.edit.others"),
    NTE_CLEAR_SELF("nametagedit.clear.self"),
    NTE_CLEAR_OTHERS("nametagedit.clear.others"),
    NTE_GROUPS("nametagedit.groups"),
    NTE_RELOAD("nametagedit.reload");

    private String permission;

    NametagEditPerms(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission);
    }
}