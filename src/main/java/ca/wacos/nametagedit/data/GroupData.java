package ca.wacos.nametagedit.data;

public class GroupData {

    private String groupName;
    private String prefix;
    private String suffix;
    private String permission;

    public GroupData() {

    }

    public GroupData(String groupName, String prefix, String suffix, String permission) {
        this.groupName = groupName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.permission = permission;
    }

    public String getName() {
        return groupName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getPermission() {
        return permission;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
